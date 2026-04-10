package com.wecureit.admin.repository;

import com.wecureit.admin.entity.DoctorFacilityAuthorization;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorFacilityAuthorizationRepository extends JpaRepository<DoctorFacilityAuthorization, Long> {
    @Modifying
    @Transactional
    void deleteByDoctorId(Long doctorId);
}
