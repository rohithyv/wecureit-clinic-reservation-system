package dev.wecureit.cdex.repo;
import dev.wecureit.cdex.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PatientRepository extends JpaRepository<Patient,String> {  }
