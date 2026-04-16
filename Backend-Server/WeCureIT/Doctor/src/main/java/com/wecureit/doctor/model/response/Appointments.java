package com.wecureit.doctor.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointments {
    // Used for grouped-by-day list
    private String day;
    private List<AppointmentDetails> appointments;

    // Used for upcoming appointment summary
    private Long appointmentId;
    private String patientName;
    private LocalDate visitDate;
    private LocalTime startTime;
    private String status;
}
