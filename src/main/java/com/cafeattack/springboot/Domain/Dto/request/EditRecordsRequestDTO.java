package com.cafeattack.springboot.Domain.Dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditRecordsRequestDTO {
    private Date recorddate;
    private String recordtext;
}
