package com.wecureit.doctor.model.request;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChamberRequest {
    private Long facilityId;
    private Long roomId;
    private String slotStart;
    private String slotEnd;
    private Integer slotDurationMinutes;
    private Double consultationCharge;
    private List<String> availableDays;
}