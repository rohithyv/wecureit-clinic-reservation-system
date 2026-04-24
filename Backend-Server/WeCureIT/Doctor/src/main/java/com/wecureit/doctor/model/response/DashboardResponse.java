package com.wecureit.doctor.model.response;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Profile profile;
    private Map<String, List<AppointmentDetails>> appointments;
    private List<Appointments> upcomingAppointments;
}
