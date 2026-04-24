package com.wecureit.doctor.service;

import com.wecureit.doctor.entity.*;
import com.wecureit.doctor.model.response.*;
import com.wecureit.doctor.repository.*;
import com.wecureit.login.model.SessionData;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final AppointmentRepository appointmentRepo;
    private final DoctorRepository doctorRepo;
    private final DoctorSpecialityLicenseRepository licenseRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final BigDecimal OVERAGE_PER_MINUTE = new BigDecimal("2.00");

    private SessionData getSession(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) return null;
        return (SessionData) redisTemplate.opsForValue().get(sessionKey);
    }

    private Doctor getLoggedInDoctor(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) {
            return doctorRepo.findFirstByOrderByIdAsc()
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
        }

        SessionData session = getSession(sessionKey);
        if (session == null) {
            return doctorRepo.findFirstByOrderByIdAsc()
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
        }

        Long doctorId = Long.parseLong(session.getId());
        return doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public DashboardResponse buildDashboardData(String sessionKey) {
        Doctor doctor = getLoggedInDoctor(sessionKey);
        Long doctorId = doctor.getId();

        Profile profile = new Profile();
        profile.setDoctorId(doctorId);
        profile.setBio(doctor.getBio());
        profile.setDegree(doctor.getDegree());
        profile.setFullName(doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());

        List<DoctorSpecialityLicense> licenses = licenseRepo.findByDoctorId(doctorId);
        Map<String, List<String>> specMap = licenses.stream().collect(
                Collectors.groupingBy(
                        DoctorSpecialityLicense::getSpecialty,
                        Collectors.mapping(DoctorSpecialityLicense::getStateCode, Collectors.toList())
                )
        );

        profile.setSpecializations(
                specMap.entrySet().stream()
                        .map(e -> new Specialization(e.getKey(), e.getValue()))
                        .toList()
        );

        List<Appointment> todayList = appointmentRepo.findTodayByDoctorId(doctorId, LocalDate.now());
        Map<String, List<AppointmentDetails>> appointmentsMap = new LinkedHashMap<>();
        appointmentsMap.put(
                LocalDate.now().toString(),
                todayList.stream().map(this::mapToDetails).toList()
        );

        List<Appointment> upcomingList = appointmentRepo.findUpcomingByDoctorId(doctorId, LocalDate.now().plusDays(1));
        List<Appointments> upcoming = upcomingList.stream().map(a -> {
            Appointments apt = new Appointments();
            apt.setAppointmentId(a.getId());
            apt.setPatientName(a.getPatient().getUser().getFirstName() + " " + a.getPatient().getUser().getLastName());
            apt.setVisitDate(a.getVisitDate());
            apt.setStartTime(a.getScheduledStartTime());
            apt.setStatus(a.getStatus());
            return apt;
        }).toList();

        return new DashboardResponse(profile, appointmentsMap, upcoming);
    }

    @Transactional
    public Appointment startAppointment(String sessionKey, Long appointmentId) {
        Doctor doctor = getLoggedInDoctor(sessionKey);
        Long doctorId = doctor.getId();

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized: Not your appointment");
        }

        appointment.setActualStartTime(OffsetDateTime.now());
        appointment.setStatus("IN_PROGRESS");
        return appointmentRepo.save(appointment);
    }

    @Transactional
    public Appointment completeAppointment(String sessionKey, Long appointmentId, String noteContent) {
        Doctor doctor = getLoggedInDoctor(sessionKey);
        Long doctorId = doctor.getId();

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized: Not your appointment");
        }

        OffsetDateTime actualEnd = OffsetDateTime.now();
        appointment.setActualEndTime(actualEnd);
        appointment.setStatus("COMPLETED");

        if (appointment.getActualStartTime() != null) {
            var scheduledEnd = appointment.getVisitDate()
                    .atTime(appointment.getScheduledEndTime())
                    .atOffset(actualEnd.getOffset());

            if (actualEnd.isAfter(scheduledEnd)) {
                long extraMinutes = java.time.Duration.between(scheduledEnd, actualEnd).toMinutes();
                appointment.setOverageFee(OVERAGE_PER_MINUTE.multiply(BigDecimal.valueOf(extraMinutes)));
            }
        }

        if (noteContent != null && !noteContent.isBlank()) {
            AppointmentNote note = new AppointmentNote();
            note.setAppointment(appointment);
            note.setDoctor(appointment.getDoctor());
            note.setPatient(appointment.getPatient());
            note.setContent(noteContent);
            note.setCreatedAt(OffsetDateTime.now());

            if (appointment.getNotes() != null) {
                appointment.getNotes().add(note);
            }
        }

        return appointmentRepo.save(appointment);
    }

    public List<AppointmentDetails> getMyAppointments(String sessionKey) {
        Doctor doctor = getLoggedInDoctor(sessionKey);
        return appointmentRepo.findByDoctorId(doctor.getId())
                .stream()
                .map(this::mapToDetails)
                .toList();
    }

    public Profile getProfile(String sessionKey) {
        Doctor doctor = getLoggedInDoctor(sessionKey);
        Long doctorId = doctor.getId();

        Profile profile = new Profile();
        profile.setDoctorId(doctorId);
        profile.setBio(doctor.getBio());
        profile.setDegree(doctor.getDegree());
        profile.setFullName(doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());

        List<DoctorSpecialityLicense> licenses = licenseRepo.findByDoctorId(doctorId);
        Map<String, List<String>> specMap = licenses.stream().collect(
                Collectors.groupingBy(
                        DoctorSpecialityLicense::getSpecialty,
                        Collectors.mapping(DoctorSpecialityLicense::getStateCode, Collectors.toList())
                )
        );

        profile.setSpecializations(
                specMap.entrySet().stream()
                        .map(e -> new Specialization(e.getKey(), e.getValue()))
                        .toList()
        );

        return profile;
    }

    private AppointmentDetails mapToDetails(Appointment a) {
        BigDecimal total = a.getBaseAmount()
                .add(a.getPenaltyFee() != null ? a.getPenaltyFee() : BigDecimal.ZERO)
                .add(a.getOverageFee() != null ? a.getOverageFee() : BigDecimal.ZERO);

        String facility = (a.getRoom() != null && a.getRoom().getFacility() != null)
                ? a.getRoom().getFacility().getFacilityName()
                : "";

        String room = (a.getRoom() != null) ? a.getRoom().getRoomNumber() : "";

        String patient = a.getPatient().getUser().getFirstName() + " " + a.getPatient().getUser().getLastName();

        return new AppointmentDetails(
                a.getId(), patient, facility, room, a.getVisitDate(),
                a.getScheduledStartTime(), a.getScheduledEndTime(), a.getStatus(),
                a.getBaseAmount(), a.getPenaltyFee(), a.getOverageFee(), total
        );
    }
}
