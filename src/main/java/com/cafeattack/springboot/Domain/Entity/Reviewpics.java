package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@NoArgsConstructor
@Entity
public class Reviewpics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewpicsid;

    @Column(nullable = false)
    private String picurl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewid", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    @Builder
    public Reviewpics(Review review, String picurl) {
        this.review = review;
        this.picurl = picurl;
    }
}
