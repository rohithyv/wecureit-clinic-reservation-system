package com.wecureit.doctor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "doctor_availability_schedule", schema = "wecureit")
public class Chamber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "day_of_cycle")
    private Integer dayOfCycle;

    @Column(name = "start_time")
    private LocalTime slotStart;

    @Column(name = "end_time")
    private LocalTime slotEnd;

    @Column(name = "slot_duration_minutes")
    private Integer slotDurationMinutes;

    @Column(name = "consultation_charge", precision = 10, scale = 2)
    private BigDecimal consultationCharge;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chamber_available_days", schema = "wecureit",
                    joinColumns = @JoinColumn(name = "chamber_id"))
    @Column(name = "day_id")
    private List<String> availableDays;
}
