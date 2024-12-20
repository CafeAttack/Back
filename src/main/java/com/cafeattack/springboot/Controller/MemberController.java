package com.cafeattack.springboot.Controller;

import com.cafeattack.springboot.Domain.Dto.request.*;
import com.cafeattack.springboot.Exception.BadRequestException;
import com.cafeattack.springboot.Service.MemberService;
import com.cafeattack.springboot.common.BaseErrorResponse;
import com.cafeattack.springboot.common.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;
import com.cafeattack.springboot.Domain.Dto.request.AuthRequestDto;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 회원가입
    @PostMapping("/auth/signup")
    public BaseResponse signup(@RequestBody AuthRequestDto authRequestDto) {
        memberService.join(authRequestDto);
        return new BaseResponse(HttpStatus.OK.value(), "회원가입이 완료되었습니다.");
    }

    // 이메일 중복 체크
    @PostMapping("/auth/email-duplication")
    public BaseResponse duplicateEmail(@RequestBody authDuplicationRequestDto AuthDuplicationRequestDto) {
        System.out.println("이메일 중복 체크 시작");
        memberService.validateEmailRequest(AuthDuplicationRequestDto.getEmail());
        return new BaseResponse(HttpStatus.OK.value(), "사용 가능한 이메일");
    }

    // 이메일 인증
    @PostMapping("/auth/email-verification")
    public BaseResponse verificationEmail(@RequestBody authVerificationEmailRequestDto AuthVerificationEmailRequestDto) {
        memberService.verifyEmailCode(AuthVerificationEmailRequestDto.getEmail(), AuthVerificationEmailRequestDto.getCode());
        return new BaseResponse(HttpStatus.OK.value(), "사용자 이메일 인증 성공");
    }

    // 로그인
    @PostMapping("/auth/login")
    public BaseResponse login(@RequestBody authLoginRequestDto AuthLoginRequestDto) {
        return new BaseResponse(HttpStatus.OK.value(), "로그인 성공",
                memberService.login(AuthLoginRequestDto.getSignId(), AuthLoginRequestDto.getPassword()));
    }

    // 로그아웃
    @DeleteMapping("/member/{memberid}/logout")
    public ResponseEntity logout(HttpServletRequest servletRequest) {
        return memberService.logout(servletRequest);
    }

    // 회원탈퇴
    @DeleteMapping("/member/{memberid}/signout")
    public ResponseEntity signout(@PathVariable Integer memberid) {
        return memberService.signout(memberid);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    BaseErrorResponse handleBadRequestException(Exception e) {
        return new BaseErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @GetMapping("/member/{memberid}/menu")
    public ResponseEntity menu_Page(@PathVariable("memberid") int memberid) {
        return memberService.menu_Page(memberid);
    }

    @GetMapping("/member/{memberid}/reset-info")
    public ResponseEntity resetinfo_Page(@PathVariable("memberid") int memberid) {
        return memberService.reset_Info(memberid);
    }

    @PatchMapping("/member/{memberid}/reset-info")
    public ResponseEntity change_info(@PathVariable("memberid") int memberid
            , @RequestBody changeInfoRequestDto ChangeInfoRequestDto) {
        return memberService.change_Info(memberid, ChangeInfoRequestDto);
    }


    @GetMapping("/member/{memberid}/personal-policy")
    public ResponseEntity PersonalPolicy() {
        return memberService.PersonalPolicy_Page();
    }

    @GetMapping("/member/{memberid}/location-policy")
    public ResponseEntity LocationPolicy() {
        return memberService.LocationPolicy_Page();
    }

    @GetMapping("/member/{memberid}/handling-policy")
    public ResponseEntity HandlingPolicy() {
        return memberService.HandlingPolicy_Page();
    }


}