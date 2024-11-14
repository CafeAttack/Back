package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Domain.Entity.mapping.CafeCategoryPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CafeCategoryPKRepository extends JpaRepository<CafeCategoryPK,Integer> {
    @Query("SELECT c.category FROM CafeCategoryPK c where c.cafe.cafeId = :cafeid")
    List<Integer> findAllCategory(@Param("cafeid") Integer cafeid);

    @Query("SELECT c.cafe from CafeCategoryPK c where c.category.category = :categoryid ")
    List<Cafe> findByCategoryId(@Param("categoryid") int categoryid);
}
