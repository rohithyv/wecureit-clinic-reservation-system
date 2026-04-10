package com.wecureit.admin.service;

import com.wecureit.admin.entity.*;
import com.wecureit.admin.model.*;
import com.wecureit.admin.repository.*;
import com.wecureit.login.entity.User;
import com.wecureit.login.entity.UserRole;
import com.wecureit.login.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepo;
    private final DoctorRepository doctorRepo;
    private final FacilityRepository facilityRepo;
    private final DoctorFacilityAuthorizationRepository doctorFacilityAuthorizationRepository;
    private final FacilityRoomRepository roomRepo;
    private final DoctorSpecialityLicenseRepository licenseRepo;
    private final DoctorAvailabilityScheduleRepository scheduleRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public Boolean onboardDoctor(OnboardDoctorRequest request) {
        try {
            var rawPassword = UUID.randomUUID().toString().substring(0, 8);
            var user = modelMapper.map(request, User.class);
            user.setRole(UserRole.DOCTOR);
            user.setPassword(passwordEncoder.encode(rawPassword));
            var savedUser = userRepo.save(user);
            var doctor = new Doctor();
            doctor.setUser(savedUser);
            var savedDoctor = doctorRepo.save(doctor);

            var licenseList = request.getLicenses().stream()
                    .flatMap(info -> info.getStateCode().stream()
                            .map(state -> {
                                DoctorSpecialityLicense l = new DoctorSpecialityLicense();
                                l.setDoctor(savedDoctor);
                                l.setSpecialty(info.getSpecialty());
                                l.setStateCode(state);
                                return l;
                            }))
                    .collect(Collectors.toList());
            licenseRepo.saveAll(licenseList);

            var authorizations = request.getPreferredFacilityIds().stream().map(facilityId -> {
                DoctorFacilityAuthorization auth = new DoctorFacilityAuthorization();
                auth.setDoctor(savedDoctor);
                auth.setFacility(facilityRepo.getReferenceById(facilityId));
                return auth;
            }).collect(Collectors.toList());
            doctorFacilityAuthorizationRepository.saveAll(authorizations);

            var start = LocalTime.parse(request.getPreferredStartTime());
            var end = LocalTime.parse(request.getPreferredEndTime());
            var fullSchedule = IntStream.rangeClosed(1, 14)
                    .mapToObj(day -> {
                        DoctorAvailabilitySchedule s = new DoctorAvailabilitySchedule();
                        s.setDoctor(savedDoctor);
                        s.setDayOfCycle(day);
                        s.setStartTime(start);
                        s.setEndTime(end);
                        return s;
                    })
                    .collect(Collectors.toList());
            scheduleRepo.saveAll(fullSchedule);

            System.out.println("--------------------------------------------------");
            System.out.println("New doctor added");
            System.out.println("Email: " + request.getEmail());
            System.out.println("Temp Password: " + rawPassword);
            System.out.println("--------------------------------------------------");

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Transactional
    public Boolean updateDoctorDetails(Long id, OnboardDoctorRequest request) {
        try {
            var doctor = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
            modelMapper.map(request, doctor.getUser());
            modelMapper.map(request, doctor);

            if (request.getLicenses() != null) {
                licenseRepo.deleteByDoctorId(id);
                var newLicenses = request.getLicenses().stream()
                        .flatMap(info -> info.getStateCode().stream().map(state -> {
                            var license = new DoctorSpecialityLicense();
                            license.setDoctor(doctor);
                            license.setSpecialty(info.getSpecialty());
                            license.setStateCode(state);
                            return license;
                        })).collect(Collectors.toList());
                licenseRepo.saveAll(newLicenses);
            }

            if (request.getPreferredFacilityIds() != null) {
                doctorFacilityAuthorizationRepository.deleteByDoctorId(id);
                var newAuthorizations = request.getPreferredFacilityIds().stream().map(fId -> {
                            var auth = new DoctorFacilityAuthorization();
                            auth.setDoctor(doctor);
                            auth.setFacility(facilityRepo.getReferenceById(fId));
                            return auth;
                }).collect(Collectors.toList());
                doctorFacilityAuthorizationRepository.saveAll(newAuthorizations);
            }
            userRepo.save(doctor.getUser());
            doctorRepo.save(doctor);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Transactional
    public Boolean deleteDoctor(Long id) {
        try {
            var doctor = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));

            if (doctor.getUser().getRole() == UserRole.DOCTOR) {
                userRepo.deleteById(doctor.getUser().getId());
                return true;
            }

            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    public Doctor getDoctorDetails(Long id) {
        return doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    @Transactional
    public Boolean onboardFacility(OnboardFacilityRequest request) {
        try {
            var facility = new Facility();
            facility.setFacilityName(request.getFacilityName());
            facility.setAddress(request.getAddress());
            facility.setState(request.getState());
            facility.setOpenTime(LocalTime.parse(request.getOpenTime()));
            facility.setCloseTime(LocalTime.parse(request.getCloseTime()));
            var savedFacility = facilityRepo.save(facility);
            if (request.getRooms() != null && !request.getRooms().isEmpty()) {
                List<FacilityRoom> roomEntities = request.getRooms().stream()
                        .map(roomReq -> {
                            FacilityRoom room = new FacilityRoom();
                            room.setFacility(savedFacility); // Link to the new facility
                            room.setRoomNumber(roomReq.getRoomNumber());
                            room.setAssignedSpecialty(roomReq.getAssignedSpecialty());
                            return room;
                        })
                        .collect(Collectors.toList());

                roomRepo.saveAll(roomEntities);
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Transactional
    public Boolean updateFacilityDetails(Long id, OnboardFacilityRequest request) {
        try {
            var facility = facilityRepo.findById(id).orElseThrow(() -> new RuntimeException("Facility not found"));
            modelMapper.map(request, facility);
            facilityRepo.save(facility);

            if (request.getRooms() != null) {
                List<Long> incomingRoomIds = request.getRooms().stream()
                        .map(RoomDetails::getRoomId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (incomingRoomIds.isEmpty()) {
                    roomRepo.deleteByFacilityId(id);
                } else {
                    roomRepo.deleteByFacilityIdAndIdNotIn(id, incomingRoomIds);
                }
                Map<Long, FacilityRoom> existingRoomsMap = roomRepo.findAllById(incomingRoomIds).stream()
                        .collect(Collectors.toMap(FacilityRoom::getId, room -> room));
                List<FacilityRoom> roomsToSave = new ArrayList<>();

                for (var roomReq : request.getRooms()) {
                    if (roomReq.getRoomId() != null && existingRoomsMap.containsKey(roomReq.getRoomId())) {
                        var room = existingRoomsMap.get(roomReq.getRoomId());
                        room.setRoomNumber(roomReq.getRoomNumber());
                        room.setAssignedSpecialty(roomReq.getAssignedSpecialty());
                        roomsToSave.add(room);
                    } else {
                        var newRoom = new FacilityRoom();
                        newRoom.setFacility(facility);
                        newRoom.setRoomNumber(roomReq.getRoomNumber());
                        newRoom.setAssignedSpecialty(roomReq.getAssignedSpecialty());
                        roomsToSave.add(newRoom);
                    }
                }
                roomRepo.saveAll(roomsToSave);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Transactional
    public Boolean deleteFacility(Long id) {
        try {
            facilityRepo.deleteById(id);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<Facility> getAllFacilities() {
        try {
            return facilityRepo.findAll();
        } catch (Exception ex) {
            ex.printStackTrace();
            return List.of(new Facility());
        }
    }

    public Facility getFacilityDetails(Long id) {
        return facilityRepo.findById(id).orElseThrow(() -> new RuntimeException("Facility not found"));
    }

    @Transactional
    public Boolean addRoomToFacility(Long facilityId, RoomRequest request) {
        try {
            Facility facility = facilityRepo.findById(facilityId).orElseThrow(() -> new RuntimeException("Facility not found"));
            FacilityRoom room = new FacilityRoom();
            room.setFacility(facility);
            room.setRoomNumber(request.getRoomNumber());
            room.setAssignedSpecialty(request.getAssignedSpecialty());
            roomRepo.save(room);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Transactional
    public Boolean updateRoomDetails(Long roomId, RoomRequest request) {
        try {
            FacilityRoom room = roomRepo.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
            room.setRoomNumber(request.getRoomNumber());
            room.setAssignedSpecialty(request.getAssignedSpecialty());
            roomRepo.save(room);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Transactional
    public Boolean deleteFacilityRoomById(Long id) {
        try {
            roomRepo.deleteById(id);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public RoomResponse getRoomById(Long roomId) {
        FacilityRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return new RoomResponse(
                room.getId(),
                room.getFacility().getId(),
                room.getRoomNumber(),
                room.getAssignedSpecialty()
        );
    }

    public List<RoomResponse> getAllRoomsByFacility(Long facilityId) {
        return roomRepo.findByFacilityId(facilityId).stream()
                .map(room -> new RoomResponse(
                        room.getId(),
                        room.getFacility().getId(),
                        room.getRoomNumber(),
                        room.getAssignedSpecialty()
                ))
                .collect(Collectors.toList());
    }

}