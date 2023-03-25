package com.jwt.jwtexample.api.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpDto {

    private String email;
    private String password;

    @Builder
    public UserSignUpDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
