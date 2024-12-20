package com.cafeattack.springboot.Controller;

import com.cafeattack.springboot.Domain.Dto.request.writeReviewRequestDto;
import com.cafeattack.springboot.Domain.Dto.response.*;
import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Repository.ReviewRepository;
import com.cafeattack.springboot.Service.ReviewService;
import com.cafeattack.springboot.common.BaseErrorResponse;
import com.cafeattack.springboot.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    @GetMapping("/{memberId}/{cafeId}")
    public ResponseEntity GetReview(@PathVariable int memberId, @PathVariable int cafeId) {
        try {
            GetReviewWriteResponseDTO data = reviewService.getReviewWriting(memberId, cafeId);

            return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(HttpStatus.OK.value(), "리뷰 작성이 완료되었습니다.", data));

        } catch (BaseException e) {
            return ResponseEntity.status(e.getCode()).body(new BaseErrorResponse(e.getCode(), e.getMessage()));
        }
    }

    @PostMapping(value = "/{memberid}/{cafeid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> writeReview(@PathVariable("memberid") int memberid,
                                         @PathVariable("cafeid") int cafeid,
                                         @RequestParam("reviewText") String reviewText,
                                         @RequestParam("reviewScore") Integer reviewScore,
                                         @RequestParam(value = "images", required = false)MultipartFile[] images) {
        try {
            writeReviewRequestDto WriteReviewRequestDto = writeReviewRequestDto.builder()
                    .reviewText(reviewText)
                    .reviewScore(reviewScore)
                    .images(images).build();

            reviewResponseDto ReviewResponseDto = reviewService.writeReviews(memberid, cafeid, WriteReviewRequestDto);

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
