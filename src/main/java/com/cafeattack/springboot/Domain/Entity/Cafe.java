package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Cafe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cafeId;

    @Column(nullable = false)
    private String cafeName;

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

    @Column
    private Integer review_cnt;  // 이게 필요할까...

    @Column
    private BigDecimal avg_score;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bookmark")
    private Bookmark bookmark;
}
