package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.type.descriptor.jdbc.SmallIntJdbcType;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer review_id;

    @Column(nullable = false)
    private Integer cafe_id;

    @Column(nullable = false)
    private String reviewWriter;

    @Column(nullable = false)
    private Date reviewTime;

    @Column(nullable = false)
    private String reviewText;

    @Column(nullable = false)
    private Integer reviewScore;

    @Column
    private Integer reviewPic;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cafe cafe;

}
