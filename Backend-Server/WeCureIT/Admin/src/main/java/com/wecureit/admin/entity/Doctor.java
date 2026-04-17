package com.wecureit.admin.entity;

import com.wecureit.login.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity(name = "AdminDoctor")
@Table(name = "doctors", schema = "wecureit")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "text")
    private String bio;

    private OffsetDateTime createdAt = OffsetDateTime.now();
}