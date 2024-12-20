package com.cafeattack.springboot.Domain.Entity;

import com.cafeattack.springboot.Domain.Dto.request.AuthRequestDto;
import com.cafeattack.springboot.Exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer memberId;

    @Column(nullable = false, length = 20)
    private String signId;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false, length = 30)
    private String email;

    @Column(length = 6)
    private String varifyingNumbers;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Date birth;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Records> recordList = new ArrayList<>();

    @Builder
    public Member(String signId, String name, String nickname,
                  String email, String password, Date birth) {
        this.signId = signId;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.birth = birth;
    }

    @Builder
    public Member(AuthRequestDto authRequestDto) {
        this.signId = authRequestDto.getSignId();
        this.name = authRequestDto.getName();
        this.nickname = authRequestDto.getNickname();
        this.email = authRequestDto.getEmail();
        this.password = authRequestDto.getPassword();
        this.birth = authRequestDto.getBirth();
    }

    public UsernamePasswordAuthenticationToken getAuthenticationToken() {
        return new UsernamePasswordAuthenticationToken(signId, password);
    }

    public int getMemberid() {
        return memberId;
    }

    public void validatePassword(String password) {
        if (!password.equals(this.password)) {
            throw new BadRequestException("비밀번호 일치하지 않음");
        }
    }
}
