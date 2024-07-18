package com.cafeattack.springboot.Domain.Entity;


import com.cafeattack.springboot.Domain.Dto.request.GroupRequestDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@Entity
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int group_id;

    @Column(nullable = false)
    private String group_name;

    @Column(nullable = true, unique = false)
    private int cafe_id;

    public Group(String group_name) {
        this.group_name = group_name;
    }

    public Group(GroupRequestDto dto) {
        this.group_name = dto.getGroupName();
    }

}
