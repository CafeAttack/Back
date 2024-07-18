package com.cafeattack.springboot.Exception;

// 특정한 상황에서 잘못된 요청이 발생했음을 나타냄
public class BadRequestException extends RuntimeException{
    public BadRequestException(String msg) {
        super(msg);
    }
}