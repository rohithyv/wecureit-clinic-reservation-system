package com.wecureit.doctor.service;

import com.wecureit.doctor.model.request.AvailabilityRequest;
import com.wecureit.doctor.model.response.DoctorScheduleResponse;
import com.wecureit.doctor.model.response.EligibleLocationResponse;
import com.wecureit.doctor.model.response.FacilityDetails;
import com.wecureit.doctor.model.response.RoomDetails;
import com.wecureit.doctor.repository.DoctorAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    
    public List<EligibleLocationResponse> findEligibleRooms(Long doctorId) {
        List<Map<String, Object>> rawData = doctorAvailabilityRepository.findEligibleRoomsByDoctorSpecialty(doctorId);

        Map<String, List<Map<String, Object>>> groupedByState = rawData.stream()
                .collect(Collectors.groupingBy(row -> (String) row.get("state")));

        return groupedByState.entrySet().stream().map(stateEntry -> {
            EligibleLocationResponse stateRes = new EligibleLocationResponse();
            stateRes.setState(stateEntry.getKey());

            Map<Long, List<Map<String, Object>>> groupedByFac = stateEntry.getValue().stream()
                    .collect(Collectors.groupingBy(row -> ((Number) row.get("facility_id")).longValue()));

            List<FacilityDetails> facilities = groupedByFac.values().stream().map(facRows -> {
                Map<String, Object> first = facRows.get(0);
                FacilityDetails facilityDetails = new FacilityDetails();
                facilityDetails.setFacilityId(((Number) first.get("facility_id")).longValue());
                facilityDetails.setFacilityName((String) first.get("facility_name"));
                facilityDetails.setAddress((String) first.get("address"));

                List<RoomDetails> rooms = facRows.stream().map(row -> {
                    RoomDetails roomDetails = new RoomDetails();
                    roomDetails.setRoomId(((Number) row.get("room_id")).longValue());
                    roomDetails.setRoomNumber((String) row.get("room_number"));
                    return roomDetails;
                }).collect(Collectors.toList());

                facilityDetails.setRooms(rooms);
                return facilityDetails;
            }).collect(Collectors.toList());

            stateRes.setFacilities(facilities);
            return stateRes;
        }).collect(Collectors.toList());
    }

    public List<DoctorScheduleResponse> getDoctorSchedule(Long doctorId) {
        List<Map<String, Object>> rawData = doctorAvailabilityRepository.findScheduleDetailsByDoctor(doctorId);
        Map<String, DoctorScheduleResponse> groupedSchedules = rawData.stream().map(row -> {
            DoctorScheduleResponse res = new DoctorScheduleResponse();
            res.setScheduleId(((Number) row.get("scheduleid")).longValue());
            res.setFacilityName((String) row.get("facilityname"));
            res.setAddress((String) row.get("address"));
            res.setRoomNumber((String) row.get("roomnumber"));
            res.setStartTime(((java.sql.Time) row.get("starttime")).toLocalTime());
            res.setEndTime(((java.sql.Time) row.get("endtime")).toLocalTime());
            res.setDays(new java.util.ArrayList<>());
            res.getDays().add((Integer) row.get("day"));
            return res;
        }).collect(Collectors.toMap(
                facility -> facility.getFacilityName() + facility.getRoomNumber() + facility.getStartTime() + facility.getEndTime(),
                facility -> facility,
                (existing, replacement) -> {
                    existing.getDays().add(replacement.getDays().getFirst());
                    return existing;
                }
        ));

        List<DoctorScheduleResponse> schedules = new java.util.ArrayList<>(groupedSchedules.values());

        for (DoctorScheduleResponse response : schedules) {
            response.setReserved(doctorAvailabilityRepository.hasBookingsInCycle(response.getScheduleId()));
        }

        return schedules;
    }

    public boolean isShiftTooLong(LocalTime start, LocalTime end) {
        if (start == null || end == null) return true;
        long minutes = Duration.between(start, end).toMinutes();
        return minutes > 480;
    }

    public boolean hasConflict(AvailabilityRequest request) {
        for (Integer day : request.getDays()) {
            if (doctorAvailabilityRepository.isDoctorAtAnotherFacility(request.getDoctorId(), day, request.getFacilityId())) {
                return true;
            }
            if (doctorAvailabilityRepository.isRoomOccupied(request.getRoomId(), day, request.getStartTime(), request.getEndTime(), request.getScheduleId())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasActiveBookingsInCycle(Long scheduleId) {
        return doctorAvailabilityRepository.hasBookingsInCycle(scheduleId);
    }

    @Transactional
    public void createNewSchedule(AvailabilityRequest request) {
        for (Integer day : request.getDays()) {
            doctorAvailabilityRepository.insertSchedule(
                    request.getDoctorId(),
                    request.getFacilityId(),
                    request.getRoomId(),
                    day,
                    request.getStartTime(),
                    request.getEndTime()
            );
        }
    }

    @Transactional
    public void updateSchedule(AvailabilityRequest request) {
        doctorAvailabilityRepository.deleteByScheduleId(request.getScheduleId());
        createNewSchedule(request);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        doctorAvailabilityRepository.deleteByScheduleId(scheduleId);
    }
}