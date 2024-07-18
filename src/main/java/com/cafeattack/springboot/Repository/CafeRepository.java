package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Cafe;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeRepository extends JpaRepository<Cafe, Integer> {
}
