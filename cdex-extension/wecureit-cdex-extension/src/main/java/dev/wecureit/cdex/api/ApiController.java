package dev.wecureit.cdex.api;
import dev.wecureit.cdex.model.*;
import dev.wecureit.cdex.repo.*;
import dev.wecureit.cdex.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api") public class ApiController {
 private final PatientRepository patients; private final ClinicalRecordRepository records; private final ExchangeRequestRepository requests;
 private final IdentityService identity; private final ExchangeService exchange;
 public ApiController(PatientRepository patients,ClinicalRecordRepository records,ExchangeRequestRepository requests,IdentityService identity,ExchangeService exchange){this.patients=patients;this.records=records;this.requests=requests;this.identity=identity;this.exchange=exchange;}
 @PostMapping("/identity/match") public IdentityService.MatchResult match(@Valid @RequestBody Requests.Match body){return identity.match(body);}
 @GetMapping("/eligibility/{memberId}/{patientId}") public ExchangeService.Eligibility eligibility(@PathVariable String memberId,@PathVariable String patientId){return exchange.eligibility(memberId,patientId);}
 @PostMapping("/exchanges") public ExchangeRequest create(@Valid @RequestBody Requests.CreateExchange body){return exchange.create(body);}
 @GetMapping("/exchanges/{id}") public ExchangeRequest request(@PathVariable String id){return requests.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @GetMapping("/records/{patientId}") public List<ClinicalRecord> records(@PathVariable String patientId){return records.findByPatientId(patientId);}
}
