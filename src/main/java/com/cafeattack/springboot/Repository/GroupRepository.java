package com.cafeattack.springboot.Repository;

import com.cafeattack.springboot.Domain.Entity.Group;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Integer>{
}
