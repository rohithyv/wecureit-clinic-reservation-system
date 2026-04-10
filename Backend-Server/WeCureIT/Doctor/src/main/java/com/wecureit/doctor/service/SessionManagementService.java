package com.wecureit.doctor.service;

import com.wecureit.doctor.repository.DoctorRepository;
import com.wecureit.login.entity.User;
import com.wecureit.login.model.SessionData;
import com.wecureit.login.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionManagementService {
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final long SESSION_EXPIRY = 7 * 24 * 60 * 60; // 7 Days

    public String getSessionKey(User user) {
        var sessionKey = UUID.randomUUID().toString();
        var sessionKeyLocator = "WE_CURE_IT_USER_" + user.getEmail();
        var id = "";

        if ("PATIENT".equalsIgnoreCase(user.getRole())) {
            id = patientRepo.findByUserId(user.getId())
                    .map(patient -> patient.getId().toString()).orElse("");
        } else if("DOCTOR".equalsIgnoreCase(user.getRole())) {
            id = doctorRepo.findByUserId(user.getId())
                    .map(doctor -> doctor.getId().toString()).orElse("");
        }
        var data = new SessionData(
                user.getEmail(),
                user.getMobileNumber(),
                user.getFirstName() + " " + user.getLastName(),
                user.getRole(),
                id
        );
        redisTemplate.opsForValue().set(sessionKey, data, SESSION_EXPIRY, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(sessionKeyLocator, sessionKey, SESSION_EXPIRY, TimeUnit.SECONDS);
        return sessionKey;
    }

    public Object getExistingSessionKey(String userSessionKey) {
        return redisTemplate.opsForValue().get(userSessionKey);
    }

    public SessionData getSession(String sessionKey) {
        if (sessionKey == null) return null;
        return (SessionData) redisTemplate.opsForValue().get(sessionKey);
    }

    public boolean deleteSession(String sessionKey) {
        if (sessionKey != null) return redisTemplate.delete(sessionKey);
        else return false;
    }

    public String createCookie(String key, long maxAge) {
        return ResponseCookie.from("session_key", key)
                .httpOnly(true).secure(false).path("/").maxAge(maxAge).sameSite("Lax").build().toString();
    }

}