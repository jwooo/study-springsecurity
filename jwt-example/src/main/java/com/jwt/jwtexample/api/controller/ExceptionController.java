package com.jwt.jwtexample.api.controller;

import com.jwt.jwtexample.api.exception.MyServiceException;
import com.jwt.jwtexample.api.response.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(MyServiceException.class)
    public ResponseEntity<ErrorResponse> myServiceException(MyServiceException e) {
        int statusCode = e.getStatusCode();

        ErrorResponse body = ErrorResponse.builder()
                .code(String.valueOf(statusCode))
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(statusCode)
                .body(body);
    }
}
