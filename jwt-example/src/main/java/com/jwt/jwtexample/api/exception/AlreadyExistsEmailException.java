package com.jwt.jwtexample.api.exception;

public class AlreadyExistsEmailException extends MyServiceException {

    private static final String MESSAGE = "이미 가입된 이메일 입니다.";

    public AlreadyExistsEmailException() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
