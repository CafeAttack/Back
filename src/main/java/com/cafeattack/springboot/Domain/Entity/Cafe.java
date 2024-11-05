package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
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

    @Column(nullable = false, length = 50)
    private String cafeName;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false)
    private String time;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column
    private Integer reviewCnt;  // 이게 필요할까...

    @Column
    private BigDecimal avgScore;

    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL)
    private List<Records> recordsList = new ArrayList<>();
}
