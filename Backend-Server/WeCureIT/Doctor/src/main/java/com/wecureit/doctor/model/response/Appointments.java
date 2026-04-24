package com.wecureit.doctor.model.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointments {
    private Long appointmentId;
    private String patientName;
    private LocalDate visitDate;
    private LocalTime startTime;
    private String status;
}
