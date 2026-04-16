package com.wecureit.doctor.repository;

import com.wecureit.doctor.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // All appointments for a doctor
    List<Appointment> findByDoctorId(Long doctorId);

    // Today's appointments for a doctor
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.visitDate = :today AND a.status <> 'CANCELLED'")
    List<Appointment> findTodayByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("today") LocalDate today);

    // Upcoming appointments from a given date onwards
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.visitDate >= :fromDate AND a.status = 'CONFIRMED' " +
           "ORDER BY a.visitDate ASC, a.scheduledStartTime ASC")
    List<Appointment> findUpcomingByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("fromDate") LocalDate fromDate);

    // All appointments by status
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);
}
