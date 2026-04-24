package com.wecureit.doctor.service;

import com.wecureit.login.model.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionManagementService {
    private final RedisTemplate<String, Object> redisTemplate;

    public SessionData getSession(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) return null;
        return (SessionData) redisTemplate.opsForValue().get(sessionKey);
    }
}
