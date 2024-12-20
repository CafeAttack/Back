package com.cafeattack.springboot.Domain.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CafeLocationDto {
    private Integer cafeId;
    private Double latitude;
    private Double longitude;
}
