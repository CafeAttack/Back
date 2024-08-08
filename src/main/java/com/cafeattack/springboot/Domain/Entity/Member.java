package com.cafeattack.springboot.Domain.Entity;

import com.cafeattack.springboot.Domain.Dto.request.AuthRequestDto;
import com.cafeattack.springboot.Exception.BadRequestException;
import com.cafeattack.springboot.Repository.MemberRepository;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

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
    private Integer memberid;

    @Column(nullable = false)
    private String signid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "birth", nullable = false)
    private Date birth;

    @Builder
    public Member(String signId, String name, String nickname,
                  String email, String password, Date birth) {
        this.signid = signId;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.birth = birth;
    }

    @Builder
    public Member(AuthRequestDto authRequestDto) {
        this.signid = authRequestDto.getSignId();
        this.name = authRequestDto.getName();
        this.nickname = authRequestDto.getNickname();
        this.email = authRequestDto.getEmail();
        this.password = authRequestDto.getPassword();
        this.birth = authRequestDto.getBirth();
    }

    public UsernamePasswordAuthenticationToken getAuthenticationToken() {
        return new UsernamePasswordAuthenticationToken(signid, password);
    }

    public int getMemberid() {
        return memberid;
    }

    public void validatePassword(String password) {
        if (!password.equals(this.password)) {
            throw new BadRequestException("비밀번호 일치하지 않음");
        }
    }
}
