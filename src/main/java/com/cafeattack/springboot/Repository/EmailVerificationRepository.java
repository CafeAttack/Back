package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
    @Query(value = "SELECT * FROM email_verification WHERE email = :email ORDER BY expiration_time DESC LIMIT 1", nativeQuery = true)
    Optional<EmailVerification> findLatestByEmailNative(@Param("email") String email);
}
