package com.cafeattack.springboot.Controller;

import com.cafeattack.springboot.Domain.Dto.request.writeReviewRequestDto;
import com.cafeattack.springboot.Domain.Dto.response.*;
import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Repository.ReviewRepository;
import com.cafeattack.springboot.Service.ReviewService;
import com.cafeattack.springboot.common.BaseErrorResponse;
import com.cafeattack.springboot.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    @PostMapping(value = "/{memberid}/{cafeid}")
    public ResponseEntity writeReview(@PathVariable("memberid") int memberid,
                                      @PathVariable("cafeid") int cafeid,
                                      @RequestBody writeReviewRequestDto WritereviewRequestDto) {
        try {
            reviewResponseDto ReviewResponseDto = reviewService.writeReviews(memberid, cafeid, WritereviewRequestDto);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new BaseResponse<>(HttpStatus.OK.value(), "리뷰 작성이 완료되었습니다.", ReviewResponseDto));
        } catch (BaseException e) {
            return ResponseEntity
                    .status(e.getCode())
                    .body(new BaseErrorResponse(e.getCode(), e.getMessage()));
        }
    }
}
