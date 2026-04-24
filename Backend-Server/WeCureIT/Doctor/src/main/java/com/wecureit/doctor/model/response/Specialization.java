package com.wecureit.doctor.model.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Specialization {
    private String specialty;
    private List<String> states;
}
