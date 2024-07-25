package com.cafeattack.springboot.Domain.Dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {
    private String signId;
    private String name;
    private String nickname;
    private String email;
    private String password;
    private String checkPassword;  // entity에는 없지만 필요함

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy.MM.dd")
    private Date birth;

    private boolean agreement;  // entity에는 없지만 필요함
}
