package com.cafeattack.springboot.Domain.Entity;

import com.cafeattack.springboot.Domain.Entity.mapping.GroupCafePK;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"groupid", "cafeid"})})
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int groupId;

    @Column
    public Integer memberId;

    @Column(length = 30)
    public String groupName;

    @OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL)
    private List<GroupCafePK> groupCafePKList = new ArrayList<>();
}