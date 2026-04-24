package com.example.core.api.guardrails.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final StringRedisTemplate redisTemplate;
    @Scheduled(fixedRate=300000)
    public void notificationSchedule() {
        log.info("Notification Service is running...");

        Set<String> keys=pendingKeys();

    }
    private Set<String> pendingKeys(){
        Set<String> keys=new HashSet<>();
        ScanOptions options=ScanOptions.scanOptions().match("user:*:pening").build();
        redisTemplate.execute((RedisCallback<Void>)connection->{
            try(Cursor<byte[]> cursor=connection.scan(options)) {
                while(cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            }catch(Exception e){
                log.error("error scanning redis ",e);
            }
            return null;

        });
        return keys;
    }

}
