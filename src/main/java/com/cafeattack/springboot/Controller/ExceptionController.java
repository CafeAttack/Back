package com.cafeattack.springboot.Controller;

import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.common.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionController {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseErrorResponse> handleException(Exception e) {
        BaseException exception = new BaseException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getMessage());
        return ResponseEntity.status(exception.getCode()).body(new BaseErrorResponse(exception));
    }
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseErrorResponse> handleBaseException(BaseException e) {
        return ResponseEntity.status(e.getCode()).body(new BaseErrorResponse(e.getCode(), e.getMessage()));
    }
}
