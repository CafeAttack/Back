package com.cafeattack.springboot.Service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cafeattack.springboot.Domain.Dto.request.writeReviewRequestDto;
import com.cafeattack.springboot.Domain.Dto.response.reviewResponseDto;
import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Domain.Entity.Review;
import com.cafeattack.springboot.Domain.Entity.Reviewpics;
import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Repository.MemberRepository;
import com.cafeattack.springboot.Repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    @Transactional
    public reviewResponseDto writeReviews(int memberid, int cafeid, writeReviewRequestDto WriteReviewRequestDto) {

        Member member = memberRepository.findById(memberid)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "Member Not Found"));

        // 닉네임 가져오기
        String nickname = member.getNickname();

        // 현재 날짜 가져오기
        LocalDate currentDate = LocalDate.now();

        // images 저장 링크 받아오기
        String[] imageUrls = s3Service.uploadPics(WriteReviewRequestDto.getImages());

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

        // S3에 이미지 업로드 및 URL 저장
        if (imageUrls != null && imageUrls.length > 0){
            if (imageUrls.length > 5) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "사진은 최대 5장까지 업로드 가능합니다");
            }
            for (String imageUrl : imageUrls) {
                Reviewpics reviewpics = Reviewpics.builder()
                        .review(saveReview)
                        .picurl(imageUrl).build();
                saveReview.getReviewpics().add(reviewpics);

            }
        }
        // DB에 저장
        reviewRepository.save(saveReview);

        return reviewResponseDto.builder()
                .reviewId(saveReview.getReviewid())
                .build();
    }
}
