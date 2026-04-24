package com.wecureit.doctor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "appointments", schema = "wecureit")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    private FacilityRoom room;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(length = 50)
    private String status;

    @Column(name = "base_amount", precision = 10, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "penalty_fee", precision = 10, scale = 2)
    private BigDecimal penaltyFee;

    @Column(name = "overage_fee", precision = 10, scale = 2)
    private BigDecimal overageFee;

    @Column(name = "duration_dr_min")
    private Integer durationDrMin;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "scheduled_start_time")
    private LocalTime scheduledStartTime;

    @Column(name = "scheduled_end_time")
    private LocalTime scheduledEndTime;

    @Column(name = "actual_start_time")
    private OffsetDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private OffsetDateTime actualEndTime;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppointmentNote> notes;
}
