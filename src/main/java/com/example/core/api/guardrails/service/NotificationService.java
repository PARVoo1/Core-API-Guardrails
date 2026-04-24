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
import java.util.List;
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
        for(String key:keys){
            processNotification(key);
        }

    }
    private Set<String> pendingKeys(){
        Set<String> keys=new HashSet<>();
        ScanOptions options=ScanOptions.scanOptions().match("user:*:pending").build();
        redisTemplate.execute((RedisCallback<Void>)connection->{
            try(Cursor<byte[]> cursor=connection.keyCommands().scan(options)) {
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
    private void processNotification(String keys){
        Long count= redisTemplate.opsForList().size(keys);
        if(count==null || count<=0){
            return;
        }
        List<String> message=redisTemplate.opsForList().leftPop(keys,count);
        if(message==null||message.isEmpty()){
            return;
        }
        String firstMessage=message.getFirst();
        if(message.size()==1){

            log.info("Push Notification Sent to User: {}",firstMessage);

        }
        else{
            long otherCount=(long) message.size()-1;
            log.info("Summarized Push Notification: {} and {} others interacted with your posts",firstMessage,otherCount);
        }

    }


}
