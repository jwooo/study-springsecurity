package com.jwt.jwtexample.api.security.jwt.utils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenDto {

    private String accessToken;
    private String refreshToken;
}
