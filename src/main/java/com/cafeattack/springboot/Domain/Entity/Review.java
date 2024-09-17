package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewid;

    @Column(nullable = false)
    private Integer cafeid;

    @Column(nullable = false)
    private String reviewwriter;

    @Column(nullable = false)
    private LocalDate reviewdate;

    @Column
    private String reviewtext;

    @Column(nullable = false)
    private Integer reviewscore;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Reviewpics> reviewpics = new ArrayList<>();

    @Builder
    public Review(Integer cafeid, String reviewwriter, LocalDate reviewdate, String reviewtext, Integer reviewscore) {
        this.cafeid = cafeid;
        this.reviewwriter = reviewwriter;
        this.reviewdate = reviewdate;
        this.reviewtext = reviewtext;
        this.reviewscore = reviewscore;
    }
}
