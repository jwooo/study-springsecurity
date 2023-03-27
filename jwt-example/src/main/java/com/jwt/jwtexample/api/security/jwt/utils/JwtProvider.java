package com.jwt.jwtexample.api.security.jwt.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_OK;

@Component
public class JwtProvider {

    private final Key secretKey;
    private final Long accessTokenExpirationPeriod;
    private final String accessHeader;
    private final Long refreshTokenExpirationPeriod;
    private final String refreshHeader;

    private static final String BEARER = "Bearer ";

    public JwtProvider(@Value("${jwt.secretKey}") String secretKey,
                       @Value("${jwt.access.expiration}") Long accessTokenExpirationPeriod,
                       @Value("${jwt.access.header}") String accessHeader,
                       @Value("${jwt.refresh.expiration}") Long refreshTokenExpirationPeriod,
                       @Value("${jwt.refresh.header}") String refreshHeader) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationPeriod  = accessTokenExpirationPeriod;
        this.accessHeader = accessHeader;
        this.refreshTokenExpirationPeriod = refreshTokenExpirationPeriod;
        this.refreshHeader = refreshHeader;
    }

    public String createAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(expireTime(accessTokenExpirationPeriod))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken() {
        return Jwts.builder()
                .setExpiration(expireTime(refreshTokenExpirationPeriod))
                .signWith(secretKey)
                .compact();
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    public void sendAccessTokenAndRefreshToken(HttpServletResponse response,
                                               String accessToken,
                                               String refreshToken) {
        response.setStatus(SC_OK);
        response.setHeader(accessHeader, accessToken);
        response.setHeader(refreshHeader, refreshToken);
    }

    public Claims getJwtTokenBody(String token) {
        return getParser().parseClaimsJws(token)
                .getBody();
    }

    public boolean isValidToken(String token) {
        try {
            getParser().parseClaimsJws(token).getBody();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private JwtParser getParser() {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build();
    }

    private Date expireTime(Long expirationPeriod) {
        return new Date(System.currentTimeMillis() + expirationPeriod);
    }
}
