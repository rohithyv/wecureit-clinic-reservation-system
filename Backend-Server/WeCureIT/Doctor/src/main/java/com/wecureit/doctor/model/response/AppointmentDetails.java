package com.wecureit.doctor.model.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDetails {
    private Long id;
    private String patientName;
    private String facilityName;
    private String roomNumber;
    private LocalDate visitDate;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;
    private String status;
    private BigDecimal baseAmount;
    private BigDecimal penaltyFee;
    private BigDecimal overageFee;
    private BigDecimal totalAmount;
}
