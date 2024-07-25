package com.cafeattack.springboot.Controller;

import com.cafeattack.springboot.Domain.Dto.request.*;
import com.cafeattack.springboot.Exception.BadRequestException;
import com.cafeattack.springboot.Service.MemberService;
import com.cafeattack.springboot.common.BaseErrorResponse;
import com.cafeattack.springboot.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
}