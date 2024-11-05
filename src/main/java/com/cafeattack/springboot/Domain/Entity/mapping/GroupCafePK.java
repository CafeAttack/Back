package com.cafeattack.springboot.Domain.Entity.mapping;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class GroupCafePK implements Serializable {
    private Integer groupid;
    private Integer cafeid;
}
