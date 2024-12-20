package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Domain.Entity.Member;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Integer> {
    @Query("SELECT c.cafeName FROM Cafe c WHERE c.cafeId = :cafeid")
    String getCafeNameByCafeid(@Param("cafeid") Integer cafeid);

    @Query("SELECT c FROM Cafe c WHERE c.cafeId = :cafeid")
    Optional<Cafe> getCafeByCafeid(@Param("cafeid") Integer cafeid);

    @Query(value = "SELECT cafe_id, latitude, longitude " +
            "FROM cafe " +
            "WHERE 6371000 * 2 * ASIN(SQRT(" +
            "POWER(SIN((RADIANS(:latitude) - RADIANS(latitude)) / 2), 2) + " +
            "COS(RADIANS(:latitude)) * COS(RADIANS(latitude)) * " +
            "POWER(SIN((RADIANS(:longitude) - RADIANS(longitude)) / 2), 2))) <= 2500",
            nativeQuery = true)
    List<Object[]> findCafesWithinRadius(@Param("latitude") double latitude, @Param("longitude") double longitude);

    @Query(value = "SELECT c.cafe_id, c.latitude, c.longitude " +
            "FROM cafe c " +
            "JOIN cafe_categorypk cc ON c.cafe_id = cc.cafe_id " +
            "WHERE cc.category = :categoryId " +
            "AND 6371000 * 2 * ASIN(SQRT( " +
            "POWER(SIN((RADIANS(:latitude) - RADIANS(c.latitude)) / 2), 2) + " +
            "COS(RADIANS(:latitude)) * COS(RADIANS(c.latitude)) * " +
            "POWER(SIN((RADIANS(:longitude) - RADIANS(c.longitude)) / 2), 2))) <= 2500",
            nativeQuery = true)
    List<Object[]> findCafesByCategoryAndRadius(@Param("categoryId") int categoryId,
                                                @Param("longitude") double longitude,
                                                @Param("latitude") double latitude);


}
