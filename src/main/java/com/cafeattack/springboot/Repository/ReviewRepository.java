package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Review;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer>{
}
