package com.wecureit.doctor.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Profile profile;
    // Today's appointments grouped by date: {"2024-03-15": [appt1, appt2]}
    private Map<String, List<AppointmentDetails>> appointments;
    // Upcoming appointments list
    private List<Appointments> upcomingAppointments;
}
