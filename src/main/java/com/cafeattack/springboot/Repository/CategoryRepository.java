package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query("SELECT c.relation.category FROM Category c where c.relation.cafeid = :cafeid")
    List<Integer> findAllCategory(@Param("cafeid") Integer cafeid);
}
