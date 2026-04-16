package com.wecureit.doctor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appointments", schema = "wecureit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private FacilityRoom room;

    @Column(name = "duration_in_min")
    private String durationInMin;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "scheduled_start_time", nullable = false)
    private LocalTime scheduledStartTime;

    @Column(name = "scheduled_end_time", nullable = false)
    private LocalTime scheduledEndTime;

    private OffsetDateTime actualStartTime;
    private OffsetDateTime actualEndTime;

    private String status = "CONFIRMED";

    // Financial tracking
    private BigDecimal baseAmount  = BigDecimal.ZERO;
    private BigDecimal penaltyFee  = BigDecimal.ZERO;
    private BigDecimal overageFee  = BigDecimal.ZERO;

    private OffsetDateTime cancelledAt;

    // Clinical notes linked to this appointment
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppointmentNote> notes = new ArrayList<>();
}
