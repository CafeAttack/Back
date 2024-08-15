package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Config.Jwt.JwtTokenProvider;
import com.cafeattack.springboot.Domain.Dto.request.*;
import com.cafeattack.springboot.Domain.Dto.response.*;
import com.cafeattack.springboot.Domain.Entity.*;
import com.cafeattack.springboot.Exception.BadRequestException;
import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Repository.BookmarkRepository;
import com.cafeattack.springboot.Repository.CafeRepository;
import com.cafeattack.springboot.Repository.CategoryRepository;
import com.cafeattack.springboot.Repository.MemberRepository;
import com.cafeattack.springboot.common.BaseErrorResponse;
import com.cafeattack.springboot.common.BaseResponse;
import com.cafeattack.springboot.common.RandomKey;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.yaml.snakeyaml.representer.BaseRepresenter;

import java.awt.print.Book;
import java.sql.SQLOutput;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CafeRepository cafeRepository;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService;

    // email 인증 코드
    private String emailCode;

    // jwt 토큰 관련
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public void join(AuthRequestDto authRequestDto) {
        validateAuthReqeust(authRequestDto);
        validateDuplicateEmail(authRequestDto.getEmail());  // 이 자리에 있는게 맞나
        memberRepository.save(convertToMember(authRequestDto));
    }

    // 이메일 인증
    @Transactional
    public void verifyEmailCode (String code) {
        if (isEmailVerified(code)) return;
        throw new BadRequestException("인증번호가 일치하지 않습니다.");
    }

    // 로그인
    @Transactional (readOnly = true)
    public authLoginResponse login(String singId, String password) {
        Member member = getMemberById(singId);
        member.validatePassword(password);
        return new authLoginResponse(member.getName()
                        , jwtTokenProvider.generateToken(member));
    }

    @Transactional
    public void validateEmailRequest(String email) {
        System.out.println("1. validateEmailRequest 시작");
        validateDuplicateEmail(email);
        createVerificationCode();
        System.out.println("3. createVerificationCode 완료");
        emailService.sendValidateEmailRequestMessage(email, getVerificationCode());
    }

    private boolean isEmailVerified(String code) {
        return emailCode.equals(code);
    }

    private void createVerificationCode() {
        emailCode = RandomKey.createKey();
    }

    private String getVerificationCode() {
        return emailCode;
    }

    private void validateDuplicateEmail(String email) {
        System.out.println("2. validateDuplicateEmail 시작");
        if (!memberRepository.findByEmail(email).stream().toList().isEmpty()) {  // 수정해야하나...?
            throw new BadRequestException("이미 회원가입된 이메일");
        }
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

    @Transactional(readOnly = true)
    public ResponseEntity menu_Page(Integer member_id) {
        Member member = memberRepository.findById(member_id).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400,"오류가 발생하였습니다"));

        int favor_count = 0;
        favor_count = (int) bookmarkRepository.countByMemberId(member_id);

        menuPageRequestDto MenuPageRequestDto = menuPageRequestDto.builder()
                .nickname(member.getName())
                .favor_count(favor_count).build();

        return ResponseEntity.status(200).body(new BaseResponse(200, "메뉴가 열렸습니다.", MenuPageRequestDto));
    }

    @Transactional(readOnly = true)
    public ResponseEntity reset_Info(Integer member_id) {
        Member member = memberRepository.findById(member_id).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "정보를 불러오는 도중 오류가 발생하였습니다."));

        memberPageResponseDto MemberPageResponseDto = memberPageResponseDto.builder()
                .signid(member.getSignid())
                .name(member.getName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .birth(member.getBirth()).build();

        return ResponseEntity.status(200).body(new BaseResponse(200, MemberPageResponseDto));
    }

    @Transactional
    public ResponseEntity change_Info(Integer member_id, changeInfoRequestDto ChangeInfoRequestDto) {
        String password = ChangeInfoRequestDto.getPassword().trim();
        String CheckPassword = ChangeInfoRequestDto.getCheckPassword().trim();
        if(!password.equals(CheckPassword))
            return ResponseEntity.status(401).body(new BaseResponse(401, "비밀번호가 일치하지 않습니다."));

        Member member = memberRepository.findById(member_id).get();
        if(member == null)
            return ResponseEntity.status(400).body(new BaseResponse(400, "해당 ID에 맞는 User가 없습니다."));

        member.setNickname(ChangeInfoRequestDto.getNickname());
        member.setPassword(ChangeInfoRequestDto.getPassword());

        return ResponseEntity.status(200).body(new BaseResponse(200, "수정이 완료되었습니다."));
    }

    @Transactional
    public ResponseEntity PersonalPolicy_Page() {
        return ResponseEntity.status(200).body(new BaseResponse(200, "페이지를 불러옵니다."));
    }

    @Transactional
    public ResponseEntity LocationPolicy_Page() {
        return ResponseEntity.status(200).body(new BaseResponse(200, "페이지를 불러옵니다."));
    }

    @Transactional
    public ResponseEntity HandlingPolicy_Page() {
        return ResponseEntity.status(200).body(new BaseResponse(200, "페이지를 불러옵니다."));
    }

    private Member getMemberById(String signId) {
        return memberRepository.findBySignid(signId).stream()
                .findFirst()
                .orElseThrow(()->new BadRequestException("회원가입되지 않은 이메일"));
    }

    // 로그아웃
    @Transactional
    public ResponseEntity logout(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization");
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseErrorResponse(HttpStatus.FORBIDDEN.value(), "유효하지 않은 Access Token"));
        }

        try {
            jwtTokenProvider.logoutToken(accessToken);
        } catch (BaseException baseException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseErrorResponse(HttpStatus.FORBIDDEN.value(), baseException.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse(HttpStatus.OK.value(), "로그아웃 완료"));
    }

    // 회원탈퇴
    @Transactional
    public ResponseEntity signout(Integer memberid) {
        try {
            memberRepository.deleteById(memberid);
        } catch (BaseException baseException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "회원탈퇴 실패"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse(HttpStatus.OK.value(), "회원탈퇴 완료"));
    }
}

