package com.wecureit.doctor.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDetails {
    private String appointmentId;
    private String patientName;
    private String facilityName;
    private String startTime;
    private String duration;
    private String status;
}