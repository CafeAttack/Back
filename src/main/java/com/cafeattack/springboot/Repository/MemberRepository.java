package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Member;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findBySignId(String signId);
}
