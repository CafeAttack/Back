package com.cafeattack.springboot.Domain.Entity;

import com.cafeattack.springboot.Exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.awt.print.Book;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer member_Id;

    @Column(nullable = false)
    private String sign_Id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Date birth;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Bookmark> bookmarks = new ArrayList<>();

    @Builder
    public Member(String sign_id, String name, String nickname, String email, String password, Date birth) {
        this.sign_Id = sign_id;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.birth = birth;
    }
}
