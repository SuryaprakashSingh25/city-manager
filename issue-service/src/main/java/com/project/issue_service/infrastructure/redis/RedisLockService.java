package com.project.issue_service.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisLockService {
    private final StringRedisTemplate redisTemplate;

    private static final String ISSUE_LOCK_PREFIX = "lock:issue:";

    /**
     * Try to acquire lock for given issueId.
     * @return true if lock acquired, false otherwise
     */
    public boolean acquireLock(String issueId, String staffId, long ttlSeconds){
        String key=ISSUE_LOCK_PREFIX+issueId;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key,staffId,ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * Release the lock if held by same staff.
     */
    public void releaseLock(String issueId, String staffId){
        String key=ISSUE_LOCK_PREFIX+issueId;
        String currentHolder=redisTemplate.opsForValue().get(key);
        if(staffId.equals(currentHolder)){
            redisTemplate.delete(key);
        }
    }

    /**
     * Check who holds the lock (for debugging)
     */
    public String getLockHolder(String issueId){
        return redisTemplate.opsForValue().get(ISSUE_LOCK_PREFIX+issueId);
    }

}
