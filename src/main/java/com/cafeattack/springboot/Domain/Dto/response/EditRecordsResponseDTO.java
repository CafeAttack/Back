package com.cafeattack.springboot.Domain.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditRecordsResponseDTO {
    private int recordcount;
    private Date recorddate;
    private String recordtext;
}
