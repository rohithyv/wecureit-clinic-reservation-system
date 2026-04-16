package com.wecureit.doctor.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profile {
    private Long doctorId;   // fixed: was String
    private String fullName;
    private String degree;
    private String bio;
    private List<Specialization> specializations;
}
