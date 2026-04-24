package com.example.core.api.guardrails.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final StringRedisTemplate redisTemplate;
    @Scheduled(fixedRate=300000)
    public void notificationSchedule() {
        log.info("Notification Service is running...");

        Set<String> keys = redisTemplate.keys("user:*:pending");

        if (keys == null || keys.isEmpty()) {
            log.info("No pending notifications found");
            return;
        }

        for (String key : keys) {
            Long count = redisTemplate.opsForList().size(key);
            if (count!=null && count > 0) {
                String firstMessage = redisTemplate.opsForList().index(key, 0);
                if (count == 1) {
                    log.info("Push Notification Sent to User: {}", firstMessage);
                } else {
                    long otherCount = count - 1;
                    log.info("Summarized Push Notification: {} and {} others interacted with your posts.", firstMessage, otherCount);
                }
                redisTemplate.delete(key);


            }

        }

    }
}
