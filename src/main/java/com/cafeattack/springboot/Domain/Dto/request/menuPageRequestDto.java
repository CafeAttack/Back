package com.cafeattack.springboot.Domain.Dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class menuPageRequestDto {
    private String nickname;
    private int favor_count;
}
