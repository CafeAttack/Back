package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer group_Id;

    @Column
    public Integer member_Id;

    @Column
    public String groupName;

    @Column
    public Integer cafe_Id;

    @ManyToOne(fetch = FetchType.LAZY)
    public Member member;

    @OneToMany(mappedBy = "cafe_Id", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Cafe> cafes = new ArrayList<>();
}
