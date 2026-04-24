package com.wecureit.doctor.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "facility_rooms", schema = "wecureit")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FacilityRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @Column(name = "assigned_specialty", length = 100)
    private String assignedSpecialty;
}
