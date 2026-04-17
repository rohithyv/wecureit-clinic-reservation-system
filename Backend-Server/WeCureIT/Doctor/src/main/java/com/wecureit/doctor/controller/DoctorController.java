package com.wecureit.doctor.controller;

import com.wecureit.doctor.entity.Appointment;
import com.wecureit.doctor.model.response.AppointmentDetails;
import com.wecureit.doctor.model.response.DashboardResponse;
import com.wecureit.doctor.model.response.Profile;
import com.wecureit.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // Dashboard: profile + today's appointments + upcoming appointments
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardData(
            @CookieValue(name = "session_key") String sessionKey) {
        return ResponseEntity.ok(doctorService.buildDashboardData(sessionKey));
    }

    // Get doctor profile with specializations
    @GetMapping("/profile")
    public ResponseEntity<Profile> getProfile(
            @CookieValue(name = "session_key") String sessionKey) {
        return ResponseEntity.ok(doctorService.getProfile(sessionKey));
    }

    // Start an appointment - records actual start time
    @PostMapping("/startAppointment/{appointmentId}")
    public ResponseEntity<Appointment> startAppointment(
            @CookieValue(name = "session_key") String sessionKey,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(doctorService.startAppointment(sessionKey, appointmentId));
    }

    // Complete appointment - saves clinical note + calculates overage fee
    @PostMapping("/completeAppointment/{appointmentId}")
    public ResponseEntity<Appointment> completeAppointment(
            @CookieValue(name = "session_key") String sessionKey,
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> body) {
        String note = body.getOrDefault("note", "");
        return ResponseEntity.ok(doctorService.completeAppointment(sessionKey, appointmentId, note));
    }

    // Get all appointments for logged-in doctor
    @GetMapping("/myAppointments")
    public ResponseEntity<List<AppointmentDetails>> getMyAppointments(
            @CookieValue(name = "session_key") String sessionKey) {
        return ResponseEntity.ok(doctorService.getMyAppointments(sessionKey));
    }
}
