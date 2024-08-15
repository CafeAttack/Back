package com.cafeattack.springboot.Domain.Dto.response;

import com.cafeattack.springboot.Config.Jwt.JwtToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class authLoginResponse {
    private String name;
    private JwtToken jwtToken;
}
