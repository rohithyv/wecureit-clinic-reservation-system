package com.wecureit.doctor.controller;

import com.wecureit.doctor.entity.Appointment;
import com.wecureit.doctor.model.request.CompleteAppointmentRequest;
import com.wecureit.doctor.model.response.*;
import com.wecureit.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoctorController {
    private final DoctorService doctorService;

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard(@CookieValue(name = "session_key", required = false) String sessionKey) {
        return doctorService.buildDashboardData(sessionKey);
    }

    @GetMapping("/profile")
    public Profile getProfile(@CookieValue(name = "session_key", required = false) String sessionKey) {
        return doctorService.getProfile(sessionKey);
    }

    @GetMapping("/myAppointments")
    public List<AppointmentDetails> getMyAppointments(@CookieValue(name = "session_key", required = false) String sessionKey) {
        return doctorService.getMyAppointments(sessionKey);
    }

    @PostMapping("/appointments/{id}/start")
    public Appointment startAppointment(
            @CookieValue(name = "session_key", required = false) String sessionKey,
            @PathVariable Long id
    ) {
        return doctorService.startAppointment(sessionKey, id);
    }

    @PostMapping("/appointments/{id}/complete")
    public Appointment completeAppointment(
            @CookieValue(name = "session_key", required = false) String sessionKey,
            @PathVariable Long id,
            @RequestBody CompleteAppointmentRequest request
    ) {
        return doctorService.completeAppointment(sessionKey, id, request.getNoteContent());
    }
}
