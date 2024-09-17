package com.cafeattack.springboot.Domain.Dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class writeReviewRequestDto {
    private String reviewText;
    private Integer reviewScore;
    private MultipartFile[] images;
}
