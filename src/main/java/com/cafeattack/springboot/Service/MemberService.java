package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Dto.request.menuPageRequestDto;
import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Repository.BookmarkRepository;
import com.cafeattack.springboot.Repository.MemberRepository;
import com.cafeattack.springboot.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements MapServiceImpl{
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Override
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

        return ResponseEntity.status(200).body(new BaseResponse(200, MenuPageRequestDto));
    }

    /*
    @Override
    public ResponseEntity change_Info() {
        //수정
        return ResponseEntity.status(200).body(new BaseResponse(200, "success"));
    }
*/

}
