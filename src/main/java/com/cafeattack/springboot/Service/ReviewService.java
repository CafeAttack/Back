package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Domain.Dto.request.writeReviewRequestDto;
import com.cafeattack.springboot.Domain.Dto.response.reviewResponseDto;
import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Domain.Entity.Review;
import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Repository.MemberRepository;
import com.cafeattack.springboot.Repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public reviewResponseDto writeReviews(int memberid, int cafeid, writeReviewRequestDto WriteReviewRequestDto) {

        Member member = memberRepository.findById(memberid)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "Member Not Found"));

        // 닉네임 가져오기
        String nickname = member.getNickname();

        // 현재 날짜 가져오기
        LocalDate currentDate = LocalDate.now();

        // Dto 정보로 Review 객체 생성
        Review review = Review.builder()
                .cafeid(cafeid)
                .reviewwriter(nickname)  // DB에서 조회한 닉네임 설정
                .reviewdate(currentDate)
                .reviewtext(WriteReviewRequestDto.getReviewText())
                .reviewscore(WriteReviewRequestDto.getReviewScore())
                .build();

        // Review 객체 DB에 저장, 반환된 객체 가져옴
        Review saveReview = reviewRepository.save(review);

        return reviewResponseDto.builder()
                .reviewId(saveReview.getReviewid())
                .build();
    }
}
