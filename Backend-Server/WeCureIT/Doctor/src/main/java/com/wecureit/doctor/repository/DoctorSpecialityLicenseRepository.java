package com.wecureit.doctor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorSpecialityLicenseRepository extends JpaRepository<DoctorSpecialityLicenseRepository, Long> {

}
