package com.cafeattack.springboot.Controller;

import com.cafeattack.springboot.Service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/member/{memberid}/menu")
    public ResponseEntity menu_Page(@PathVariable("memberid") int memberid) {
        return memberService.menu_Page(memberid);
    }


}
