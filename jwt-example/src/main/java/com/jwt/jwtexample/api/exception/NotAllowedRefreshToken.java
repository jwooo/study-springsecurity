package com.jwt.jwtexample.api.exception;

public class NotAllowedRefreshToken extends MyServiceException {

    private static final String MESSAGE = "허용되지 않는 리프레쉬 토큰입니다.";

    public NotAllowedRefreshToken() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
