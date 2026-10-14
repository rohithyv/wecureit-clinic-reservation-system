package dev.wecureit.cdex.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="patients") public class Patient {
 @Id public String id;
 @Column(nullable=false) public String firstName;
 @Column(nullable=false) public String lastName;
 @Column(nullable=false) public LocalDate birthDate;
 public String postalCode;
 protected Patient() {}
 public Patient(String id,String firstName,String lastName,LocalDate birthDate,String postalCode){this.id=id;this.firstName=firstName;this.lastName=lastName;this.birthDate=birthDate;this.postalCode=postalCode;}
}
