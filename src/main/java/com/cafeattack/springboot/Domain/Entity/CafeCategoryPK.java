package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Embeddable
public class CafeCategoryPK implements Serializable {
    private Integer cafeid;
    private Integer category;
}
