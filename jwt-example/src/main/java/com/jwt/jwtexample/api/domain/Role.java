package com.jwt.jwtexample.api.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    ADMIN("ROLE_ADMIN"), USER("ROLE_USER");
    private final String key;
}
