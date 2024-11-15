package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.mapping.CafeCategoryPK;
import com.cafeattack.springboot.Domain.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, CafeCategoryPK> {
}