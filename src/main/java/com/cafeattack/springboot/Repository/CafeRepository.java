package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Cafe;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CafeRepository extends JpaRepository<Cafe, Integer> {
    @Query("SELECT c.cafename FROM Cafe c WHERE c.cafeid = :cafeid")
    String getCafeNameByCafeid(@Param("cafeid") Integer cafeid);
}
