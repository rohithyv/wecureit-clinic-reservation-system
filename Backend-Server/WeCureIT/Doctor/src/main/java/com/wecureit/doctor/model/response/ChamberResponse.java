package com.wecureit.doctor.model.response;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChamberResponse {
    private Long id;
    private Long facilityId;
    private String facilityName;
    private String facilityAddress;
    private Long roomId;
    private String slotStart;
    private String slotEnd;
    private Integer slotDurationMinutes;
    private Double consultationCharge;
    private List<String> availableDays;
}