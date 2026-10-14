package dev.wecureit.cdex.service;
import dev.wecureit.cdex.api.Requests;
import dev.wecureit.cdex.model.*;
import dev.wecureit.cdex.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.*;
@Service public class ExchangeService {
 private final PatientRepository patients; private final CoverageRepository coverages;
 private final ClinicalRecordRepository documents; private final ExchangeRequestRepository requests;
 public ExchangeService(PatientRepository patients,CoverageRepository coverages,ClinicalRecordRepository documents,ExchangeRequestRepository requests){this.patients=patients;this.coverages=coverages;this.documents=documents;this.requests=requests;}
 public record Eligibility(String memberId,String patientId,boolean eligible,String coverageId) {}
 public Eligibility eligibility(String memberId,String patientId){
  LocalDate today=LocalDate.now();
  return coverages.findByMemberId(memberId).stream().filter(c->c.patientId.equals(patientId))
   .filter(c->!c.startDate.isAfter(today) && !c.endDate.isBefore(today)).findFirst()
   .map(c->new Eligibility(memberId,patientId,true,c.id))
   .orElse(new Eligibility(memberId,patientId,false,null));
 }
 @Transactional public ExchangeRequest create(Requests.CreateExchange data){
  if(!patients.existsById(data.patientId()))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Patient not found");
  Eligibility eligibility=eligibility(data.memberId(),data.patientId());
  if(!eligibility.eligible())throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"No active coverage for member/patient");
  List<ClinicalRecord> records=documents.findByPatientId(data.patientId());
  String documentId=records.isEmpty()?null:records.get(0).id;
  String status=documentId==null?"DATA_NOT_FOUND":"COMPLETED";
  return requests.save(new ExchangeRequest(UUID.randomUUID().toString(),data.memberId(),data.patientId(),eligibility.coverageId(),status,data.reason(),documentId));
 }
}
