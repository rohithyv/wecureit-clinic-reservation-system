package com.wecureit.doctor.controller;

import com.wecureit.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Map;
import com.wecureit.doctor.model.request.LogVisitRequest;
import com.wecureit.doctor.model.response.AllAppointmentsResponse;
import com.wecureit.doctor.model.response.AppointmentDetails;
import com.wecureit.doctor.model.response.Profile;

@RestController
@RequestMapping("api/v1/doctor/appointments/")
@RequiredArgsConstructor
public class DoctorAppointmentsController {
    private final DoctorService doctorService;

    @GetMapping("/getWeeklySchedule")
    public ResponseEntity<Map<String, List<AppointmentDetails>>> getWeeklySchedule(@CookieValue(name = "session_key") String sessionKey) {
        return ResponseEntity.ok(doctorService.getWeeklySchedule(sessionKey));
    }

    @GetMapping("/getDoctorProfile")
    public ResponseEntity<Profile> getDoctorProfile(@CookieValue(name = "session_key") String sessionKey) {
        return ResponseEntity.ok(doctorService.getDoctorProfile(sessionKey));
    }

    @GetMapping("/allAppointments")
    public ResponseEntity<AllAppointmentsResponse> getAllAppointments(@CookieValue(name = "session_key") String sessionKey) {
        return ResponseEntity.ok(doctorService.getAllAppointments(sessionKey));
    }

    @PostMapping("/logVisit")
    public ResponseEntity<Boolean> logVisit(@RequestBody LogVisitRequest logVisitRequest, @CookieValue(name = "session_key") String sessionKey) {
        return ResponseEntity.ok(doctorService.logVisit(logVisitRequest));
    }

}