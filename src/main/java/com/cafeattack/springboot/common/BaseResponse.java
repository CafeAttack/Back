package com.cafeattack.springboot.common;

import lombok.Getter;

@Getter
public class BaseResponse<T> {

    private final int status;
    private String message;  // 응답에 메시지 포함
    private T data;

    // status, message, data 모두 넘겨주는 response
    public BaseResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // status, message 만 넘겨주는 response
    public BaseResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // status, data 만 넘겨주는 response
    public BaseResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }
}
