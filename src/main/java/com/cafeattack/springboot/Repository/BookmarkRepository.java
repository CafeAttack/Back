package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer>{
    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.memberid = :memberid")
    long countByMemberId(@Param("memberid") Integer memberid);

    @Query("SELECT distinct b.relation.groupid FROM Bookmark b WHERE b.memberid = :memberid")
    List<Integer> findAllgroupidByMemberId(@Param("memberid") Integer memberid);

    @Query("SELECT b.relation.cafeid FROM Bookmark b WHERE b.memberid = :memberid")
    List<Integer> findAllcafeByMemberId(@Param("memberid") Integer memberid);

    @Query("SELECT DISTINCT b.groupname FROM Bookmark b WHERE b.relation.groupid = :groupid")
    String getGroupNameByGroupid(@Param("groupid") Integer groupid);

    @Query("SELECT b.relation.cafeid FROM Bookmark b WHERE b.relation.groupid = :groupid")
    List<Integer> findAllcafeByGroupid(@Param("groupid") Integer groupid);

    @Query("SELECT DISTINCT b.memberid FROM Bookmark b WHERE b.relation.groupid = :groupid")
    Integer getMemberidByGroupid(@Param("groupid") Integer groupid);

    @Query("SELECT Max(b.relation.groupid) FROM Bookmark b")
    Integer getMaxGroupid();
}
