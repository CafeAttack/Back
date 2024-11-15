package com.cafeattack.springboot.Domain.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordDateOrderPageResponseDTO {
    private String cafename;
    private Integer visitcount;
    private Date latestvisit;
}