package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Embeddable
public class CafeReviewPK implements Serializable {
    private int CafeID;
    private int ReviewID;
}
