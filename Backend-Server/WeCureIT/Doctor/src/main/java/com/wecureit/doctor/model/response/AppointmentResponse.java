package com.wecureit.doctor.model.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AppointmentResponse {
    private Long appointmentId;
    private String patientName;
    private String facilityName;
    private String roomNumber;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private String status;
    private BigDecimal baseAmount;
    private BigDecimal penaltyFee;
    private BigDecimal overageFee;
    private BigDecimal totalAmount;
    private String note;
}