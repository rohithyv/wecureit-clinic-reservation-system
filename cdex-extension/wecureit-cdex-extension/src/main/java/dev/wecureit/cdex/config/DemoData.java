package dev.wecureit.cdex.config;
import dev.wecureit.cdex.model.*;
import dev.wecureit.cdex.repo.*;
import org.springframework.boot.*;
import org.springframework.context.annotation.*;
import java.time.LocalDate;
@Configuration public class DemoData {
 @Bean @Profile("demo") CommandLineRunner seed(PatientRepository patients,CoverageRepository coverages,ClinicalRecordRepository records){return args->{
  patients.save(new Patient("PAT-100","John","Carter",LocalDate.parse("1988-04-21"),"22201"));
  coverages.save(new Coverage("COV-100","MEM-100","PAT-100","Synthetic Example Payer",LocalDate.now().minusMonths(1),LocalDate.now().plusMonths(12)));
  records.save(new ClinicalRecord("DOC-100","PAT-100","ENC-100","Synthetic clinical summary","Fictional demonstration record only. No real patient data."));
 };}
}
