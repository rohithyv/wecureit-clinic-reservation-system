package dev.wecureit.cdex.service;
import dev.wecureit.cdex.api.Requests;
import dev.wecureit.cdex.model.Patient;
import dev.wecureit.cdex.repo.PatientRepository;
import org.springframework.stereotype.Service;
import java.util.*;
@Service public class IdentityService {
 private final PatientRepository patients;
 public IdentityService(PatientRepository patients){this.patients=patients;}
 public record MatchResult(String patientId,double score,String status) {}
 public MatchResult match(Requests.Match q){
  List<Patient> candidates=patients.findAll();
  Patient best=null; double max=0;
  for(Patient p:candidates){
   double score=0;
   if(p.birthDate.equals(q.birthDate()))score+=0.40;
   if(p.lastName.equalsIgnoreCase(q.lastName().trim()))score+=0.30;
   if(p.firstName.equalsIgnoreCase(q.firstName().trim()))score+=0.20;
   if(q.postalCode()!=null && p.postalCode!=null && p.postalCode.equals(q.postalCode().trim()))score+=0.10;
   if(score>max){max=score;best=p;}
  }
  // No auto-link for ambiguous matches: caller must review anything below 0.90.
  String status=max>=0.90?"MATCHED":max>=0.60?"REVIEW":"NO_MATCH";
  return new MatchResult(best==null?null:best.id,Math.round(max*100.0)/100.0,status);
 }
}
