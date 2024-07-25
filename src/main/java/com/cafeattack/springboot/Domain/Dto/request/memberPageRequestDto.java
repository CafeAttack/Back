package com.cafeattack.springboot.Domain.Dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class memberPageRequestDto {
    private String sign_id;
    private String name;
    private String nickname;
    private String e_mail;
    private Date birth;
}
