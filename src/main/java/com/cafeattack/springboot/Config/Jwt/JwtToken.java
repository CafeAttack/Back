package com.cafeattack.springboot.Config.Jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class JwtToken {
    private String grantType;
    private Integer memberid;
    private String accessToken;
    private String refreshToken;
}
