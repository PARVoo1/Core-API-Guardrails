# Guardrails & Virality Engine

A Spring Boot backend built to handle high-concurrency bot storms while maintaining data integrity across PostgreSQL and Redis.

---

## Running Locally

**Prerequisites:** Docker, Java 17+, Maven

```bash
# 1. Spin up Postgres and Redis
docker-compose up -d

# 2. Run the app
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

The repository includes an empty `postman_collection.json` placeholder for future Postman exports/imports.

---

## Project Structure
```text
src/
├── main/
│   ├── java/com/example/core/api/guardrails/
│   │   ├── CoreApiGuardrailsApplication.java
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── application.properties
│       ├── static/
│       └── templates/
└── test/java/com/example/core/api/guardrails/
    └── CoreApiGuardrailsApplicationTests.java

docker-compose.yml
postman_collection.json
README.md
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts` | Create a post |
| POST | `/api/posts/{postId}/comments` | Add a comment (with guardrails) |
| POST | `/api/posts/{postId}/like` | Like a post |

Note: `postId` is intentionally not included in the comment request body — the URL is the source of truth for that relationship.

---

## Thread Safety: How the Atomic Lock Works (Phase 2)

This was the core problem: under a 200-request race condition, a standard database `COUNT(*)` check fails. Multiple threads read "99 comments" simultaneously, all pass the check, and you end up with 110+ rows in the DB.

**The fix: Redis `INCR`**

Redis is single-threaded internally, which makes its `INCR` command atomic by nature. Here's what happens when 200 requests hit concurrently:

1. Each request calls `redisTemplate.opsForValue().increment("comment_count:{postId}")`
2. Redis serializes all 200 increments — no two threads get the same value back
3. The Java service checks the returned value immediately:
    - If `<= 100` → proceed to save in PostgreSQL
    - If `> 100` → throw `ResponseStatusException(429)` before the DB transaction even opens

This means PostgreSQL never sees the overflow. The gate is enforced entirely in Redis, in microseconds.

**The 10-minute cooldown (bot → human)**

Implemented as a distributed lock using a Redis key with a TTL:
cooldown:bot_{botId}:human_{userId}  →  TTL: 600 seconds

On each request, the service checks `redisTemplate.hasKey(...)`. If the key exists, the request is rejected. If it doesn't, the comment goes through and the key is set with a 10-minute expiry.

One detail worth noting: the check uses `Boolean.TRUE.equals(redisTemplate.hasKey(...))` rather than a direct null check — Redis can return `null` on a connection blip, and a raw `!= null` check would mishandle that as "key exists," accidentally blocking comments.

**Transaction integrity**

Everything is wrapped in `@Transactional`. If Redis increments the counter but the PostgreSQL save fails (or vice versa), the whole operation rolls back. This prevents phantom counter increments where Redis thinks a comment exists but the DB has no record of it.

---

## Scalability

No state lives in the application. No HashMaps, no local caches — counters, cooldown locks, and notification queues all live in Redis. You can run multiple instances behind a load balancer and they'll all share the same state through the same Redis instance.

---

## Testing the Guardrails

1. **Create a post** using the `Create Post` request — save the returned `postId`
2. **Seed a parent comment** using `Create Comment` with `authorType: BOT`
3. **Stress test** using the `oha` command provided, or by running the Postman request in a loop

Once the 100-comment cap is hit, the server will consistently return `429 Too Many Requests` for all subsequent requests.