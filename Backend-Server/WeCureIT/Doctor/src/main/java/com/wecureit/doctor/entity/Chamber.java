package com.wecureit.doctor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "doctor_availability_schedule", schema = "wecureit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Chamber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "slot_start")
    private String slotStart;

    @Column(name = "slot_end")
    private String slotEnd;

    @Column(name = "slot_duration_minutes")
    private Integer slotDurationMinutes;
    
    @Column(name = "day_of_cycle")
    private Integer dayOfCycle;

    @Column(name = "consultation_charge")
    private Double consultationCharge;

    @ElementCollection
    @CollectionTable(name = "chamber_available_days",
        schema = "wecureit",
        joinColumns = @JoinColumn(name = "chamber_id"))
    @Column(name = "day_id")
    private List<String> availableDays;
}