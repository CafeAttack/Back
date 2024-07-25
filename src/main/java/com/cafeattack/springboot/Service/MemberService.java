package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Dto.request.AuthRequestDto;
import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Exception.BadRequestException;
import com.cafeattack.springboot.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // email 인증 코드 추가해야


    // jwt 토큰 관련 추가해야


    // 회원가입
    @Transactional
    public void join(AuthRequestDto authRequestDto) {
        validateAuthReqeust(authRequestDto);
        memberRepository.save(convertToMember(authRequestDto));
    }

    private void validateAuthReqeust(AuthRequestDto authRequestDto) {
        if (authRequestDto.getSignId() == null)
            throw new BadRequestException("아이디를 입력해주세요.");
        if (authRequestDto.getPassword() == null)
            throw new BadRequestException("비밀번호를 입력해주세요.");
        if (authRequestDto.getNickname() == null)
            throw new BadRequestException("닉네임을 입력해주세요.");
        if (authRequestDto.getEmail() == null)
            throw new BadRequestException("이메일을 입력해주세요.");
        if (!authRequestDto.getCheckPassword().equals(authRequestDto.getPassword()))
            throw new BadRequestException("비밀번호가 일치하지 않습니다.");
        if (authRequestDto.getBirth() == null)
            throw new BadRequestException("생년월일을 입력해주세요.");
        if (!authRequestDto.isAgreement())
            throw new BadRequestException("개인정보 수집에 동의해주세요.");
    }

    private Member convertToMember(AuthRequestDto authRequestDto) {
        return new Member(authRequestDto);
    }
}

