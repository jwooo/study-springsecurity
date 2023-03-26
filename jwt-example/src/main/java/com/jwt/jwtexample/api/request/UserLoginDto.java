package com.jwt.jwtexample.api.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginDto {

    private String email;
    private String password;
}
