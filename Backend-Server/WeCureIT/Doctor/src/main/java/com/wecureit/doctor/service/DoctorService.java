package com.wecureit.doctor.service;

import com.wecureit.doctor.entity.Appointment;
import com.wecureit.doctor.entity.AppointmentNote;
import com.wecureit.doctor.entity.DoctorSpecialityLicense;
import com.wecureit.doctor.model.response.*;
import com.wecureit.doctor.repository.AppointmentRepository;
import com.wecureit.doctor.repository.DoctorRepository;
import com.wecureit.doctor.repository.DoctorSpecialityLicenseRepository;
import com.wecureit.login.model.SessionData;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorRepository doctorRepo;
    private final DoctorSpecialityLicenseRepository licenseRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    // $2.00 overage fee per extra minute beyond scheduled end time
    private static final BigDecimal OVERAGE_PER_MINUTE = new BigDecimal("2.00");

    // ─── GET SESSION FROM REDIS ──────────────────────────────────────────────
    private SessionData getSession(String sessionKey) {
        if (sessionKey == null) return null;
        return (SessionData) redisTemplate.opsForValue().get(sessionKey);
    }

    // ─── DASHBOARD ──────────────────────────────────────────────────────────
    // Returns doctor profile + today's appointments + upcoming appointments
    public DashboardResponse buildDashboardData(String sessionKey) {
        SessionData session = getSession(sessionKey);
        if (session == null) throw new RuntimeException("Invalid session");

        Long doctorId = Long.parseLong(session.getId());

        var doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Build Profile
        var profile = new Profile();
        profile.setDoctorId(doctorId);
        profile.setBio(doctor.getBio());
        profile.setDegree(doctor.getDegree());
        profile.setFullName(doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());

        // Build Specializations from licenses
        List<DoctorSpecialityLicense> licenses = licenseRepo.findByDoctorId(doctorId);
        Map<String, List<String>> specMap = licenses.stream().collect(
                Collectors.groupingBy(DoctorSpecialityLicense::getSpecialty,
                        Collectors.mapping(DoctorSpecialityLicense::getStateCode, Collectors.toList())));
        profile.setSpecializations(
                specMap.entrySet().stream()
                        .map(e -> new Specialization(e.getKey(), e.getValue()))
                        .toList());

        // Today's appointments grouped by date
        List<Appointment> todayList = appointmentRepo.findTodayByDoctorId(doctorId, LocalDate.now());
        Map<String, List<AppointmentDetails>> appointmentsMap = new LinkedHashMap<>();
        appointmentsMap.put(LocalDate.now().toString(),
                todayList.stream().map(this::mapToDetails).toList());

        // Upcoming appointments (from tomorrow onwards)
        List<Appointment> upcomingList = appointmentRepo.findUpcomingByDoctorId(
                doctorId, LocalDate.now().plusDays(1));
        List<Appointments> upcoming = upcomingList.stream().map(a -> {
            var apt = new Appointments();
            apt.setAppointmentId(a.getId());
            apt.setPatientName(a.getPatient().getUser().getFirstName()
                    + " " + a.getPatient().getUser().getLastName());
            apt.setVisitDate(a.getVisitDate());
            apt.setStartTime(a.getScheduledStartTime());
            apt.setStatus(a.getStatus());
            return apt;
        }).toList();

        return new DashboardResponse(profile, appointmentsMap, upcoming);
    }

    // ─── START APPOINTMENT ───────────────────────────────────────────────────
    // Records actual start time, changes status to IN_PROGRESS
    @Transactional
    public Appointment startAppointment(String sessionKey, Long appointmentId) {
        SessionData session = getSession(sessionKey);
        if (session == null) throw new RuntimeException("Invalid session");

        Long doctorId = Long.parseLong(session.getId());
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId))
            throw new RuntimeException("Unauthorized: Not your appointment");

        appointment.setActualStartTime(OffsetDateTime.now());
        appointment.setStatus("IN_PROGRESS");
        return appointmentRepo.save(appointment);
    }

    // ─── COMPLETE APPOINTMENT ────────────────────────────────────────────────
    // Records actual end time, calculates overage fee, saves clinical note
    @Transactional
    public Appointment completeAppointment(String sessionKey, Long appointmentId, String noteContent) {
        SessionData session = getSession(sessionKey);
        if (session == null) throw new RuntimeException("Invalid session");

        Long doctorId = Long.parseLong(session.getId());
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId))
            throw new RuntimeException("Unauthorized: Not your appointment");

        OffsetDateTime actualEnd = OffsetDateTime.now();
        appointment.setActualEndTime(actualEnd);
        appointment.setStatus("COMPLETED");

        // Calculate overage fee if appointment ran over scheduled end time
        if (appointment.getActualStartTime() != null) {
            var scheduledEnd = appointment.getVisitDate()
                    .atTime(appointment.getScheduledEndTime())
                    .atOffset(actualEnd.getOffset());
            if (actualEnd.isAfter(scheduledEnd)) {
                long extraMinutes = java.time.Duration.between(scheduledEnd, actualEnd).toMinutes();
                appointment.setOverageFee(OVERAGE_PER_MINUTE.multiply(BigDecimal.valueOf(extraMinutes)));
            }
        }

        // Save clinical note if provided
        if (noteContent != null && !noteContent.isBlank()) {
            var note = new AppointmentNote();
            note.setAppointment(appointment);
            note.setDoctor(appointment.getDoctor());
            note.setPatient(appointment.getPatient());
            note.setContent(noteContent);
            note.setCreatedAt(OffsetDateTime.now());
            appointment.getNotes().add(note);
        }

        return appointmentRepo.save(appointment);
    }

    // ─── GET ALL MY APPOINTMENTS ─────────────────────────────────────────────
    public List<AppointmentDetails> getMyAppointments(String sessionKey) {
        SessionData session = getSession(sessionKey);
        if (session == null) throw new RuntimeException("Invalid session");

        Long doctorId = Long.parseLong(session.getId());
        return appointmentRepo.findByDoctorId(doctorId)
                .stream().map(this::mapToDetails).toList();
    }

    // ─── GET PROFILE ─────────────────────────────────────────────────────────
    public Profile getProfile(String sessionKey) {
        SessionData session = getSession(sessionKey);
        if (session == null) throw new RuntimeException("Invalid session");

        Long doctorId = Long.parseLong(session.getId());
        var doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        var profile = new Profile();
        profile.setDoctorId(doctorId);
        profile.setBio(doctor.getBio());
        profile.setDegree(doctor.getDegree());
        profile.setFullName(doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());

        List<DoctorSpecialityLicense> licenses = licenseRepo.findByDoctorId(doctorId);
        Map<String, List<String>> specMap = licenses.stream().collect(
                Collectors.groupingBy(DoctorSpecialityLicense::getSpecialty,
                        Collectors.mapping(DoctorSpecialityLicense::getStateCode, Collectors.toList())));
        profile.setSpecializations(specMap.entrySet().stream()
                .map(e -> new Specialization(e.getKey(), e.getValue())).toList());

        return profile;
    }

    // ─── MAPPER ──────────────────────────────────────────────────────────────
    private AppointmentDetails mapToDetails(Appointment a) {
        BigDecimal total = a.getBaseAmount()
                .add(a.getPenaltyFee() != null ? a.getPenaltyFee() : BigDecimal.ZERO)
                .add(a.getOverageFee() != null ? a.getOverageFee() : BigDecimal.ZERO);

        String facility = (a.getRoom() != null && a.getRoom().getFacility() != null)
                ? a.getRoom().getFacility().getFacilityName() : "";
        String room = (a.getRoom() != null) ? a.getRoom().getRoomNumber() : "";
        String patient = a.getPatient().getUser().getFirstName()
                + " " + a.getPatient().getUser().getLastName();

        return new AppointmentDetails(
                a.getId(), patient, facility, room,
                a.getVisitDate(), a.getScheduledStartTime(), a.getScheduledEndTime(),
                a.getStatus(),
                a.getBaseAmount(), a.getPenaltyFee(), a.getOverageFee(), total);
    }
}
