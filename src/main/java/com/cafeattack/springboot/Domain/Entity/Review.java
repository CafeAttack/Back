package com.cafeattack.springboot.Domain.Entity;

import com.cafeattack.springboot.Domain.Entity.mapping.ReviewTagsPK;
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
    private String reviewWriter;

    @Column(nullable = false)
    private LocalDate reviewDate;

    @Column
    private String reviewText;

    @Column(nullable = false)
    private Integer reviewScore;

    @Column(nullable = false)
    private int amenities;

    @ManyToOne
    @JoinColumn(nullable = false, name = "cafe_id")
    private Cafe cafe;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Reviewpics> reviewpicsList = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private final List<ReviewTagsPK> reviewTagsPKList = new ArrayList<>();

    @Builder
    public Review(Cafe cafe, String reviewwriter, LocalDate reviewdate, String reviewtext, Integer reviewscore) {
        this.cafe = cafe;
        this.reviewWriter = reviewwriter;
        this.reviewDate = reviewdate;
        this.reviewText = reviewtext;
        this.reviewScore = reviewscore;
    }
}
