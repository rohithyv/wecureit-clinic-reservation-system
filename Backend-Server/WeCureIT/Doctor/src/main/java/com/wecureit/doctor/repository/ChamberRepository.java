package com.wecureit.doctor.repository;

import com.wecureit.doctor.entity.Chamber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChamberRepository extends JpaRepository<Chamber, Long> {
    List<Chamber> findByDoctorId(Long doctorId);
}
