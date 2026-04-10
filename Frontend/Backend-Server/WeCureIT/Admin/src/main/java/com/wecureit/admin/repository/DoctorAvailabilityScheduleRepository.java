package com.wecureit.admin.repository;

import com.wecureit.admin.entity.DoctorAvailabilitySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorAvailabilityScheduleRepository extends JpaRepository<DoctorAvailabilitySchedule, Long> {

}