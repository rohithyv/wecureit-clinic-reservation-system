package com.wecureit.doctor.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "doctor_specialty_licenses", schema = "wecureit")
public class DoctorSpecialityLicense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "state_code", length = 10)
    private String stateCode;

    @Column(length = 100)
    private String specialty;
}
