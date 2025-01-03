package com.cafeattack.springboot.Domain.Entity;

import com.cafeattack.springboot.Domain.Entity.mapping.CafeCategoryPK;
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

    @Column(length = 50)
    private String cafeName;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Column(length = 100)
    private String address;

    @Column
    private String time;

    @Column(length = 20)
    private String phone;

    @Column
    private Integer reviewCnt;  // 이게 필요할까...

    @Column
    private BigDecimal avgScore;

    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL)
    private List<Records> recordsList = new ArrayList<>();

    @OneToMany(mappedBy = "cafe", cascade = CascadeType.ALL)
    private List<CafeCategoryPK> cafeCategoryPKList = new ArrayList<>();
}
