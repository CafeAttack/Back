package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.CafeReview;
import jakarta.persistence.*;
import com.cafeattack.springboot.Domain.Entity.CafeReviewPK;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeReviewRepository extends JpaRepository<CafeReview, CafeReviewPK> {
}
