package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.type.descriptor.jdbc.SmallIntJdbcType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer review_id;

    @Column(nullable = false)
    private String cafe_name;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String time;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private boolean heart;

    @Column(nullable = false)
    private int review_cnt;

    @Column(nullable = false)
    private float avgScore;

    @Builder
    public Review(String name, BigDecimal latitude, BigDecimal longitude, String address, String time, String phone) {
        this.cafe_name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.time = time;
        this.phone = phone;
        this.heart = false;
        this.review_cnt = 0;
        this.avgScore = 0f;
    }
}
