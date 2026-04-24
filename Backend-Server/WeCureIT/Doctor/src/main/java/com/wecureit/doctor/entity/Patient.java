package com.wecureit.doctor.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "patients", schema = "wecureit")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "preferred_location")
    private String preferredLocation;

    @Column(name = "cardholder_name", length = 255)
    private String cardholderName;

    @Column(name = "credit_card_number", length = 20)
    private String creditCardNumber;

    @Column(name = "expiration_date", length = 7)
    private String expirationDate;

    @Column(length = 4)
    private String cvv;

    @Column(name = "billing_zip", length = 10)
    private String billingZip;

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;
}
