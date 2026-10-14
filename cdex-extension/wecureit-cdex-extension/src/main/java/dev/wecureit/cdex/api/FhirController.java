package dev.wecureit.cdex.api;
import dev.wecureit.cdex.model.*;
import dev.wecureit.cdex.repo.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping(value="/fhir/r4",produces="application/fhir+json") public class FhirController {
 private final PatientRepository patients;private final CoverageRepository coverage;private final ExchangeRequestRepository exchanges;
 public FhirController(PatientRepository patients,CoverageRepository coverage,ExchangeRequestRepository exchanges){this.patients=patients;this.coverage=coverage;this.exchanges=exchanges;}
 @GetMapping("/Patient/{id}") public Map<String,Object> patient(@PathVariable String id){
  Patient p=patients.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  return Map.of("resourceType","Patient","id",p.id,"name",List.of(Map.of("family",p.lastName,"given",List.of(p.firstName))),"birthDate",p.birthDate.toString());
 }
 @GetMapping("/Coverage/{id}") public Map<String,Object> coverage(@PathVariable String id){
  Coverage c=coverage.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  return Map.of("resourceType","Coverage","id",c.id,"status","active","beneficiary",Map.of("reference","Patient/"+c.patientId),"payor",List.of(Map.of("display",c.payer)));
 }
 @GetMapping("/Task/{id}") public Map<String,Object> task(@PathVariable String id){
  ExchangeRequest r=exchanges.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  String status=r.status.equals("COMPLETED")?"completed":r.status.equals("DATA_NOT_FOUND")?"failed":"in-progress";
  Map<String,Object> task=new LinkedHashMap<>();task.put("resourceType","Task");task.put("id",r.id);task.put("status",status);task.put("intent","order");task.put("for",Map.of("reference","Patient/"+r.patientId));task.put("description",r.reason);
  return task;
 }
}
