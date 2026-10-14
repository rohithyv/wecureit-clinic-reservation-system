package dev.wecureit.cdex.repo;
import dev.wecureit.cdex.model.ClinicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord,String> { java.util.List<ClinicalRecord> findByPatientId(String patientId); }
