package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Domain.Entity.mapping.GroupCafePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupCafePKRepository extends JpaRepository<GroupCafePK,Integer> {
    @Query("SELECT distinct b.groupId FROM GroupCafePK g join Bookmark b on g.bookmark.groupId = b.groupId WHERE b.memberId =: memberId")
    List<Integer> findAllgroupIdBymemberId(@Param("memberId") Integer memberId);

    @Query("SELECT g.cafe.cafeId FROM GroupCafePK g join Bookmark b on g.bookmark.groupId = b.groupId WHERE b.memberId = :memberId")
    List<Integer> findAllcafeBymemberId(@Param("memberId") Integer memberId);

    @Query("SELECT g.cafe.cafeId FROM GroupCafePK g WHERE g.bookmark.groupId= :groupid")
    List<Integer> findAllcafeByGroupid(@Param("groupid") Integer groupid);

    @Query("SELECT b.cafe.cafeId From GroupCafePK b where b.bookmark.groupId = :groupid")
    List<Integer> findAllcafeidByGroupid(@Param("groupid") Integer groupid);

    @Query("SELECT b.bookmark.groupId FROM GroupCafePK b WHERE b.cafe.cafeId = :cafeid")
    List<Integer> findAllgroupidByCafeid(@Param("cafeid") Integer cafeid);

    @Query("SELECT g FROM GroupCafePK g where g.cafe.cafeId = :cafeId and g.bookmark.groupId = :groupId")
    GroupCafePK findByRelation(@Param("groupId") Integer groupId, @Param("cafeId") Integer cafeId);

    @Query("SELECT g from GroupCafePK g join Bookmark b on g.bookmark = b WHERE b.memberId = :memberId and g.cafe = :cafe")
    GroupCafePK findByMemberCafe(@Param("memberId") int memberId, @Param("cafe") Cafe cafe);
}
