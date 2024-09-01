package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Reviewpics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewpicsid;

    @Column(nullable = false)
    private String picurl;

    @ManyToOne
    @JoinColumn(name = "reviewid")
    private Review review;
}
