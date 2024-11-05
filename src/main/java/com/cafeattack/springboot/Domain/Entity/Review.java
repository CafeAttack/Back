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
    private Integer reviewId;

    @Column(nullable = false)
    private Integer cafeId;

    @Column(nullable = false)
    private String reviewWriter;

    @Column(nullable = false)
    private LocalDate reviewDate;

    @Column
    private String reviewText;

    @Column(nullable = false)
    private Integer reviewScore;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Reviewpics> reviewpicsList = new ArrayList<>();

    @Builder
    public Review(Integer cafeid, String reviewwriter, LocalDate reviewdate, String reviewtext, Integer reviewscore) {
        this.cafeId = cafeid;
        this.reviewWriter = reviewwriter;
        this.reviewDate = reviewdate;
        this.reviewText = reviewtext;
        this.reviewScore = reviewscore;
    }
}
