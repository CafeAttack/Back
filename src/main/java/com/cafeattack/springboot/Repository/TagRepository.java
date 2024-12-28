package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {
}
