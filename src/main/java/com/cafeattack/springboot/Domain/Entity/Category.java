package com.cafeattack.springboot.Domain.Entity;

import com.cafeattack.springboot.Domain.Entity.mapping.CafeCategoryPK;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int category;

    @Column(nullable = false, length = 10)
    private String name;

    @ManyToOne
    @MapsId("cafeid")  // CafeCategoryPK의 cafeid와 매핑
    @JoinColumn(name = "cafeid", insertable = false, updatable = false)
    private Cafe cafe;

    public Category(CafeCategoryPK relation) {
        this.relation = relation;
    }
}