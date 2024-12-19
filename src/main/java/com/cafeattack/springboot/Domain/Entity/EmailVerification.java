package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailId;

    @Column(unique = true, nullable = false)
    private String email;
    private Integer verificationCode;
    private LocalDateTime expirationTime;

    public EmailVerification(String email, Integer verificationCode, LocalDateTime expirationTime) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.expirationTime = expirationTime;
    }

    public EmailVerification() {

    }
}
