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
    public Integer groupId;

    @Column
    public Integer memberId;

    @Column
    public String groupName;

    @Column
    public Integer cafeId;

    @ManyToOne(fetch = FetchType.LAZY)
    public Member member;

    @OneToMany(mappedBy = "cafeId", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Cafe> cafes = new ArrayList<>();
}
