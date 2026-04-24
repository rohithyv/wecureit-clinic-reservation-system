package com.wecureit.doctor.controller;

import com.wecureit.doctor.model.request.AvailabilityRequest;
import com.wecureit.doctor.service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/doctor/availability/")
@RequiredArgsConstructor
public class DoctorAvailabilityController {
    private final DoctorAvailabilityService doctorAvailabilityService;

    @GetMapping("/eligibleLocations")
    public ResponseEntity<?> getEligibleLocations(@RequestParam Long doctorId) {
        return ResponseEntity.ok(doctorAvailabilityService.findEligibleRooms(doctorId));
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSchedule(@RequestParam Long doctorId) {
        return ResponseEntity.ok(doctorAvailabilityService.getDoctorSchedule(doctorId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addAvailability(@RequestBody AvailabilityRequest request) {
        if (doctorAvailabilityService.isShiftTooLong(request.getStartTime(), request.getEndTime())) {
            return ResponseEntity.badRequest()
                    .body("Doctors can work at most 8 hours per day at one facility.");
        }
        if (doctorAvailabilityService.hasConflict(request)) {
            return ResponseEntity.badRequest()
                    .body("Schedule conflict: Selected day/time is already reserved.");
        }
        doctorAvailabilityService.createNewSchedule(request);
        return ResponseEntity.ok("Availability added successfully.");
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateAvailability(@RequestBody AvailabilityRequest request) {
        if (request.getScheduleId() == null) {
            return ResponseEntity.badRequest().body("Missing schedule identifier for update.");
        }
        if (doctorAvailabilityService.hasActiveBookingsInCycle(request.getScheduleId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Schedule Locked: Active appointments detected in the next 14 days. Contact Admin for an exception.");
        }
        if (doctorAvailabilityService.isShiftTooLong(request.getStartTime(), request.getEndTime()) ||
                doctorAvailabilityService.hasConflict(request)) {
            return ResponseEntity.badRequest().body("Invalid shift duration or schedule conflict.");
        }
        doctorAvailabilityService.updateSchedule(request);
        return ResponseEntity.ok("Schedule updated successfully.");
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeAvailability(@RequestBody Long scheduleId) {
        if (doctorAvailabilityService.hasActiveBookingsInCycle(scheduleId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Removal Blocked: Existing appointments within the 14-day cycle prevent deletion.");
        }
        doctorAvailabilityService.deleteSchedule(scheduleId);
        return ResponseEntity.ok("Shift removed. Room is now available for other providers.");
    }
}
