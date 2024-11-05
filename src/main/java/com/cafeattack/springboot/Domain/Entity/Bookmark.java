package com.cafeattack.springboot.Domain.Entity;

import com.cafeattack.springboot.Domain.Entity.mapping.GroupCafePK;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Setter
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