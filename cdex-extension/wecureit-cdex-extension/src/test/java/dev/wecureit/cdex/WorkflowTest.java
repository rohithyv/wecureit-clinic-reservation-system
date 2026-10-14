package dev.wecureit.cdex;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.wecureit.cdex.api.Requests;
import dev.wecureit.cdex.model.*;
import dev.wecureit.cdex.repo.*;
import dev.wecureit.cdex.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc class WorkflowTest {
 @Autowired PatientRepository patients; @Autowired CoverageRepository coverages; @Autowired ClinicalRecordRepository records;
 @Autowired IdentityService identity; @Autowired ExchangeService service; @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
 private void seed(){patients.save(new Patient("P1","Jane","Doe",LocalDate.of(1990,1,2),"12345"));coverages.save(new Coverage("C1","M1","P1","Demo",LocalDate.now().minusDays(1),LocalDate.now().plusDays(1)));records.save(new ClinicalRecord("D1","P1","E1","Test","Synthetic"));}
 @Test void identityFindsMatch(){seed();var match=identity.match(new Requests.Match("Jane","Doe",LocalDate.of(1990,1,2),"12345"));assertEquals("MATCHED",match.status());assertEquals("P1",match.patientId());}
 @Test void exchangeCompletesWithDocument(){seed();var result=service.create(new Requests.CreateExchange("M1","P1","CLAIMS_SUPPORT"));assertEquals("COMPLETED",result.status);assertEquals("D1",result.documentId);}
 @Test void wrongMemberIsNotEligible(){seed();assertFalse(service.eligibility("INVALID","P1").eligible());}
 @Test @WithMockUser(roles="PROVIDER") void providerCannotCreatePayerExchange() throws Exception {mvc.perform(post("/api/exchanges").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(new Requests.CreateExchange("M1","P1","CLAIMS_SUPPORT")))).andExpect(status().isForbidden());}
 @Test @WithMockUser(roles="PAYER") void payerCanReadPatientFhir() throws Exception {seed();mvc.perform(get("/fhir/r4/Patient/P1")).andExpect(status().isOk()).andExpect(jsonPath("$.resourceType").value("Patient"));}
}
