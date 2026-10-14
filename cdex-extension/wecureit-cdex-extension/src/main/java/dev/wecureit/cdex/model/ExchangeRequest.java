package dev.wecureit.cdex.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="exchange_requests") public class ExchangeRequest {
 @Id public String id;
 public String memberId;
 public String patientId;
 public String coverageId;
 public String status;
 public String reason;
 public String documentId;
 public Instant createdAt;
 protected ExchangeRequest() {}
 public ExchangeRequest(String id,String memberId,String patientId,String coverageId,String status,String reason,String documentId){this.id=id;this.memberId=memberId;this.patientId=patientId;this.coverageId=coverageId;this.status=status;this.reason=reason;this.documentId=documentId;this.createdAt=Instant.now();}
}
