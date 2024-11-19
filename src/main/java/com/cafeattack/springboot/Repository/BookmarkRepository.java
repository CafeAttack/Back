package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Bookmark;
import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Domain.Entity.mapping.GroupCafePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer>{
    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.memberId = :memberId")
    long countBymemberId(@Param("memberId") Integer memberId);

    @Query("SELECT DISTINCT b.groupName FROM Bookmark b WHERE b.groupId = :groupid")
    String getGroupNameByGroupid(@Param("groupid") Integer groupid);

    @Query("SELECT DISTINCT b.memberId FROM Bookmark b WHERE b.groupId = :groupid")
    Integer getmemberIdByGroupid(@Param("groupid") Integer groupid);

    @Query("SELECT Max(b.groupId) FROM Bookmark b")
    Integer getMaxGroupid();

    @Query("SELECT COUNT(b) FROM Bookmark b where b.groupId = :groupid")
    long countByGroupid(@Param("groupid") Integer groupid);
}
