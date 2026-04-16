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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChamberService {

    private final ChamberRepository chamberRepo;
    private final DoctorRepository doctorRepo;
    private final SessionManagementService sessionService;

    public List<ChamberResponse> getMyChambers(String sessionKey) {
        SessionData session = sessionService.getSession(sessionKey);
        Doctor doctor = doctorRepo.findByUserId(session.getUserId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return chamberRepo.findByDoctorId(doctor.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ChamberResponse addChamber(String sessionKey, ChamberRequest req) {
        SessionData session = sessionService.getSession(sessionKey);
        Doctor doctor = doctorRepo.findByUserId(session.getUserId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        Chamber chamber = new Chamber();
        chamber.setDoctor(doctor);
        chamber.setFacilityId(req.getFacilityId());
        chamber.setRoomId(req.getRoomId());
        chamber.setSlotStart(req.getSlotStart());
        chamber.setSlotEnd(req.getSlotEnd());
        chamber.setSlotDurationMinutes(req.getSlotDurationMinutes());
        chamber.setConsultationCharge(req.getConsultationCharge());
        chamber.setAvailableDays(req.getAvailableDays());
        return toResponse(chamberRepo.save(chamber));
    }

    public ChamberResponse updateChamber(String sessionKey, Long chamberId, ChamberRequest req) {
        Chamber chamber = chamberRepo.findById(chamberId)
                .orElseThrow(() -> new RuntimeException("Chamber not found"));
        chamber.setSlotStart(req.getSlotStart());
        chamber.setSlotEnd(req.getSlotEnd());
        chamber.setSlotDurationMinutes(req.getSlotDurationMinutes());
        chamber.setConsultationCharge(req.getConsultationCharge());
        chamber.setAvailableDays(req.getAvailableDays());
        return toResponse(chamberRepo.save(chamber));
    }

    public void deleteChamber(String sessionKey, Long chamberId) {
        chamberRepo.deleteById(chamberId);
    }

    private ChamberResponse toResponse(Chamber c) {
        ChamberResponse res = new ChamberResponse();
        res.setId(c.getId());
        res.setFacilityId(c.getFacilityId());
        res.setRoomId(c.getRoomId());
        res.setSlotStart(c.getSlotStart());
        res.setSlotEnd(c.getSlotEnd());
        res.setSlotDurationMinutes(c.getSlotDurationMinutes());
        res.setConsultationCharge(c.getConsultationCharge());
        res.setAvailableDays(c.getAvailableDays());
        return res;
    }
}