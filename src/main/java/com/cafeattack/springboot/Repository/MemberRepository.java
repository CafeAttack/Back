package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Member;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
}
