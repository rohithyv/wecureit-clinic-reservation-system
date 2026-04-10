package com.wecureit.admin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OnboardFacilityRequest {
    private String facilityName;
    private String address;
    private String state;
    private String openTime;
    private String closeTime;
    private List<RoomDetails> rooms;
}