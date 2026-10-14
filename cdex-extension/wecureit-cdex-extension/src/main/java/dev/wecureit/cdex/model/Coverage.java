package dev.wecureit.cdex.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="coverage") public class Coverage {
 @Id public String id;
 @Column(nullable=false) public String memberId;
 @Column(nullable=false) public String patientId;
 public String payer;
 public LocalDate startDate;
 public LocalDate endDate;
 protected Coverage() {}
 public Coverage(String id,String memberId,String patientId,String payer,LocalDate startDate,LocalDate endDate){this.id=id;this.memberId=memberId;this.patientId=patientId;this.payer=payer;this.startDate=startDate;this.endDate=endDate;}
}
