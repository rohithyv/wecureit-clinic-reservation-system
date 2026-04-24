package com.wecureit.doctor.model.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    private Long doctorId;
    private String fullName;
    private String degree;
    private String bio;
    private List<Specialization> specializations;
}
