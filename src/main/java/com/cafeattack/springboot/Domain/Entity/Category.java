package com.cafeattack.springboot.Domain.Entity;

import com.cafeattack.springboot.Domain.Entity.mapping.CafeCategoryPK;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Category {
    @EmbeddedId
    private CafeCategoryPK relation;

    @ManyToOne
    @MapsId("cafeid")  // CafeCategoryPK의 cafeid와 매핑
    @JoinColumn(name = "cafeid", insertable = false, updatable = false)
    private Cafe cafe;

    public Category(CafeCategoryPK relation) {
        this.relation = relation;
    }
}