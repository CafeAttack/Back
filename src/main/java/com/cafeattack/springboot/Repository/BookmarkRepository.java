package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Bookmark;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer>{
    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.memberid = :memberid")
    long countByMemberId(@Param("memberid") Integer memberid);
}
