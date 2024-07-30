package com.cafeattack.springboot.Domain.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class bookmarkPageCafeResponseDto {
    private Integer cafeId;
    private String cafeName;
    private List<bookmarkPageCategoryResposeDto> categories;
}
