package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Bookmark;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer>{
}
