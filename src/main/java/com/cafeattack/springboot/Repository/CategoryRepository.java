package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Cafe;
import com.cafeattack.springboot.Domain.Entity.CafeCategoryPK;
import com.cafeattack.springboot.Domain.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, CafeCategoryPK> {
    @Query("SELECT c.relation.category FROM Category c where c.relation.cafeid = :cafeid")
    List<Integer> findAllCategory(@Param("cafeid") Integer cafeid);

    @Query("SELECT c.cafe from Category c where c.relation.category = :categoryid ")
    List<Cafe> findByCategoryId(@Param("categoryid") int categoryid);
}