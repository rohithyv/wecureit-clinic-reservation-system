package com.wecureit.doctor.service;

import com.wecureit.doctor.model.response.DashboardResponse;
import com.wecureit.doctor.model.response.Profile;
import com.wecureit.doctor.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final AppointmentRepository appointmentRepo;
//    public DashboardResponse buildDashboardData(String sessionKey) {
//        var dashboardResponse = new DashboardResponse();
//        var profile = new Profile();
//        profile.setDoctorId();
//        profile.setBio();
//        profile.setDegree();
//        profile.setFullName();
//        dashboardResponse.setProfile(profile);
//        dashboardResponse.setAppointments();
//        dashboardResponse.setUpcomingAppointments();
//        return new DashboardResponse();
//    }

}
