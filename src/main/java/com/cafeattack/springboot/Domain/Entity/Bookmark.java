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
    @Column
    public Integer groupid;

    @Column
    public Integer memberid;

    @Column
    public String groupname;

    @Column
    public Integer cafeid;

    @ManyToOne(fetch = FetchType.LAZY)
    public Member member;

    @OneToMany(mappedBy = "cafeid", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Cafe> cafes = new ArrayList<>();
}
