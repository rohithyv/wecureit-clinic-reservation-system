package com.wecureit.admin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private Long roomId;
    private Long facilityId;
    private String roomNumber;
    private String assignedSpecialty;
}
