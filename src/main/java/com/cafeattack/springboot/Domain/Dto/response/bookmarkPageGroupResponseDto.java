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
public class bookmarkPageGroupResponseDto {
    private Integer groupId;
    private String groupName;
    private List<bookmarkPageCafeResponseDto> cafes;
}
