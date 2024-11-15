package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Domain.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.Date;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Integer> {

    @Query("SELECT r.cafe FROM Records r WHERE r.member =: member ")
    List<Cafe> findAllCafeByMember(@Param("member") Member member);

    @Query("SELECT count(r) FROM Records r WHERE r.member =: member and r.cafe.cafeId =: cafeId")
    int getVisitCount(@Param("member") Member member, @Param("cafeId") int cafeId);

    @Query("SELECT r.recordDate FROM Records r WHERE r.member =: member and r.cafe =: cafe order by r.recordDate DESC limit 1")
    Date getLatestDate(@Param("member") Member member, @Param("cafe") Cafe cafe);
}
