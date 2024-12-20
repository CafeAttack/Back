package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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

    @Query("SELECT b.groupId FROM Bookmark b join GroupCafePK g on b = g.bookmark WHERE b.memberId = :memberId and g.cafe.cafeId = cafeId")
    Optional<Integer> isFavorite(@Param("memberId") Integer memberId, @Param("cafeId") Integer cafeId);
}
