package com.wecureit.doctor.repository;

import com.wecureit.doctor.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorId(Long doctorId);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = ?1 AND a.visitDate = ?2")
    List<Appointment> findTodayByDoctorId(Long doctorId, LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = ?1 AND a.visitDate >= ?2")
    List<Appointment> findUpcomingByDoctorId(Long doctorId, LocalDate fromDate);
}
