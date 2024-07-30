package com.cafeattack.springboot.Domain.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"groupid", "cafeid"})})
public class Bookmark {
    @EmbeddedId
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    public GroupCafePK relation;

    @Column
    public Integer memberid;

    @Column
    public String groupname;
}
