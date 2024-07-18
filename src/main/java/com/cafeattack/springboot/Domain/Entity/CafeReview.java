package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class CafeReview {

    @EmbeddedId
    private CafeReviewPK id;

    public CafeReview(CafeReviewPK id) {
        this.id = id;
    }
}
