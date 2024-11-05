package com.cafeattack.springboot.Domain.Entity.mapping;

import com.cafeattack.springboot.Domain.Entity.Bookmark;
import com.cafeattack.springboot.Domain.Entity.Cafe;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.awt.print.Book;
import java.io.Serializable;

@Data
@Entity
@Builder
public class GroupCafePK implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Bookmark bookmark;
}
