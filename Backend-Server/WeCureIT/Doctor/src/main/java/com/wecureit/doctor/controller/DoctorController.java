package com.wecureit.doctor.controller;

import com.wecureit.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/doctor")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;

//    @GetMapping("/dashboard")
//    public ResponseEntity<?> getDashboardData(@CookieValue(name = "session_key") String sessionKey) {
//        var dashboardResponse = doctorService.buildDashboardData(sessionKey);
//    }

}