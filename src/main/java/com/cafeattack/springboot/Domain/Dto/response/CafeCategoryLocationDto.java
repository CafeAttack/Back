package com.cafeattack.springboot.Domain.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CafeCategoryLocationDto {
    private Integer cafeId;
    private double longitude;
    private double latitude;
}
