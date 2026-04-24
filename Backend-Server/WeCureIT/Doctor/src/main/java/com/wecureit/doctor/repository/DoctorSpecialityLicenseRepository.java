package com.wecureit.doctor.repository;

import com.wecureit.doctor.entity.DoctorSpecialityLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorSpecialityLicenseRepository extends JpaRepository<DoctorSpecialityLicense, Long> {
    List<DoctorSpecialityLicense> findByDoctorId(Long doctorId);
}
