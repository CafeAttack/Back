package com.cafeattack.springboot.Domain.Entity.mapping;

import com.cafeattack.springboot.Domain.Entity.Review;
import com.cafeattack.springboot.Domain.Entity.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewTagsPK {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    public ReviewTagsPK(Review review, Tag tag) {
        this.review = review;
        this.tag = tag;
    }
}
