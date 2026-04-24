package com.wecureit.doctor.service;

import com.wecureit.doctor.entity.Chamber;
import com.wecureit.doctor.entity.Doctor;
import com.wecureit.doctor.model.request.ChamberRequest;
import com.wecureit.doctor.model.response.ChamberResponse;
import com.wecureit.doctor.repository.ChamberRepository;
import com.wecureit.doctor.repository.DoctorRepository;
import com.wecureit.login.model.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChamberService {
    private final ChamberRepository chamberRepo;
    private final DoctorRepository doctorRepo;
    private final SessionManagementService sessionService;

    private Doctor getLoggedInDoctor(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) {
            return doctorRepo.findFirstByOrderByIdAsc()
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
        }

        SessionData session = sessionService.getSession(sessionKey);
        if (session == null) {
            return doctorRepo.findFirstByOrderByIdAsc()
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
        }

        Long doctorId = Long.parseLong(session.getId());
        return doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public List<ChamberResponse> getMyChambers(String sessionKey) {
        Doctor doctor = getLoggedInDoctor(sessionKey);
        return chamberRepo.findByDoctorId(doctor.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ChamberResponse addChamber(String sessionKey, ChamberRequest req) {
        Doctor doctor = getLoggedInDoctor(sessionKey);

        Chamber chamber = new Chamber();
        chamber.setDoctor(doctor);
        chamber.setFacilityId(req.getFacilityId());
        chamber.setRoomId(req.getRoomId());
        chamber.setSlotStart(LocalTime.parse(req.getSlotStart()));
        chamber.setSlotEnd(LocalTime.parse(req.getSlotEnd()));
        chamber.setSlotDurationMinutes(req.getSlotDurationMinutes());
        chamber.setConsultationCharge(req.getConsultationCharge());
        chamber.setAvailableDays(req.getAvailableDays());
        chamber.setDayOfCycle(req.getDayOfCycle());

        return toResponse(chamberRepo.save(chamber));
    }

    public ChamberResponse updateChamber(String sessionKey, Long chamberId, ChamberRequest req) {
        Doctor doctor = getLoggedInDoctor(sessionKey);

        Chamber chamber = chamberRepo.findById(chamberId)
                .orElseThrow(() -> new RuntimeException("Chamber not found"));

        if (!chamber.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Unauthorized chamber access");
        }

        chamber.setFacilityId(req.getFacilityId());
        chamber.setRoomId(req.getRoomId());
        chamber.setSlotStart(LocalTime.parse(req.getSlotStart()));
        chamber.setSlotEnd(LocalTime.parse(req.getSlotEnd()));
        chamber.setSlotDurationMinutes(req.getSlotDurationMinutes());
        chamber.setConsultationCharge(req.getConsultationCharge());
        chamber.setAvailableDays(req.getAvailableDays());
        chamber.setDayOfCycle(req.getDayOfCycle());

        return toResponse(chamberRepo.save(chamber));
    }

    public void deleteChamber(String sessionKey, Long chamberId) {
        Doctor doctor = getLoggedInDoctor(sessionKey);

        Chamber chamber = chamberRepo.findById(chamberId)
                .orElseThrow(() -> new RuntimeException("Chamber not found"));

        if (!chamber.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Unauthorized chamber access");
        }

        chamberRepo.delete(chamber);
    }

    private ChamberResponse toResponse(Chamber c) {
        ChamberResponse res = new ChamberResponse();
        res.setId(c.getId());
        res.setFacilityId(c.getFacilityId());
        res.setRoomId(c.getRoomId());
        res.setSlotStart(c.getSlotStart().toString());
        res.setSlotEnd(c.getSlotEnd().toString());
        res.setSlotDurationMinutes(c.getSlotDurationMinutes());
        res.setConsultationCharge(c.getConsultationCharge());
        res.setAvailableDays(c.getAvailableDays());
        return res;
    }
}
