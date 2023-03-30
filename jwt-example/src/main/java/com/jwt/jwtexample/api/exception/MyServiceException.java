package com.jwt.jwtexample.api.exception;

import lombok.Getter;

@Getter
public abstract class MyServiceException extends RuntimeException {

    public MyServiceException(String message) {
        super(message);
    }

    public MyServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract int getStatusCode();

}
