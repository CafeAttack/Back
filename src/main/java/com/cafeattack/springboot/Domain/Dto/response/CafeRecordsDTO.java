package com.cafeattack.springboot.Domain.Dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class CafeRecordsDTO {
    Integer id;
    Date date;
    String text;
}
