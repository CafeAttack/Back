package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Category {
    @EmbeddedId
    private CafeCategoryPK relation;

    public Category(CafeCategoryPK relation) {
        this.relation = relation;
    }
}
