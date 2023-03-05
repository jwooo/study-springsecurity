package com.example.ex2.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class OtpAuthentication extends UsernamePasswordAuthenticationToken {

    public OtpAuthentication(Object principle, Object credentials) {
        super(principle, credentials);
    }

    public OtpAuthentication(Object principle, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principle, credentials, authorities);
    }
}
