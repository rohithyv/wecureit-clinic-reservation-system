package com.wecureit.admin.repository;

import com.wecureit.admin.entity.DoctorSpecialityLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorSpecialityLicenseRepository extends JpaRepository<DoctorSpecialityLicense, Long> {
    void deleteByDoctorId(Long doctorId);

}
