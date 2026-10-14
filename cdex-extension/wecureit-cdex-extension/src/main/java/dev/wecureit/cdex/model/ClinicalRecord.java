package dev.wecureit.cdex.model;
import jakarta.persistence.*;
@Entity @Table(name="clinical_records") public class ClinicalRecord {
 @Id public String id;
 public String patientId;
 public String encounterId;
 public String title;
 @Column(length=4000) public String summary;
 protected ClinicalRecord() {}
 public ClinicalRecord(String id,String patientId,String encounterId,String title,String summary){this.id=id;this.patientId=patientId;this.encounterId=encounterId;this.title=title;this.summary=summary;}
}
