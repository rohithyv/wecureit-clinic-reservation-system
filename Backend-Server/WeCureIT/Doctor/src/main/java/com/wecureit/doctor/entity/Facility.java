package com.wecureit.doctor.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "facilities", schema = "wecureit")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "facility_name", length = 255)
    private String facilityName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 2)
    private String state;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;
}
