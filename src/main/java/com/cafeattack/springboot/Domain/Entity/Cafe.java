package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Entity
public class Cafe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cafeid;

    @Column(nullable = false)
    private String cafename;

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

    @Column
    private Integer review_cnt;  // 이게 필요할까...

    @Column
    private BigDecimal avg_score;
}
