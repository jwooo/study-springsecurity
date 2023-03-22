package com.example.personaljwtpractice.api.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.personaljwtpractice.api.repository.UserRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;

@Service
@Getter
@Slf4j
public class JwtService {

    private final String secretKey;
    private final Long accessTokenExpirationPeriod;
    private final String accessHeader;
    private final Long refreshTokenExpirationPeriod;
    private final String refreshHeader;

    private final UserRepository userRepository;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER = "Bearer ";

    public JwtService(@Value("${jwt.secretKey}") String secretKey,
                      @Value("${jwt.access.expiration}") Long accessTokenExpirationPeriod,
                      @Value("${jwt.access.header}") String accessHeader,
                      @Value("${jwt.refresh.expiration}") Long refreshTokenExpirationPeriod,
                      @Value("${jwt.refresh.header}") String refreshHeader,
                      UserRepository userRepository) {
        this.secretKey = secretKey;
        this.accessTokenExpirationPeriod = accessTokenExpirationPeriod;
        this.accessHeader = accessHeader;
        this.refreshTokenExpirationPeriod = refreshTokenExpirationPeriod;
        this.refreshHeader = refreshHeader;
        this.userRepository = userRepository;
    }

    public String createAccessToken(String email) {
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(createExpireDate(accessTokenExpirationPeriod))
                .withClaim(EMAIL_CLAIM, email)
                .sign(Algorithm.HMAC512(secretKey));
    }

    public String createRefreshToken() {
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(createExpireDate(refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
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

    public void updateRefreshToken(String email, String refreshToken) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(user -> user.updateRefreshToken(refreshToken),
                        () -> new Exception("일치하는 회원이 없습니다."));
    }

    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)
                    .getClaim(EMAIL_CLAIM)
                    .asString());
        } catch (Exception e) {
            log.error("엑세스 토큰이 유효하지 않습니다.");
            return Optional.empty();
        }
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
        log.info("AccessToken, RefreshToken 헤더 설정 완료");
    }

    private void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    private void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다.");
            return false;
        }
    }

    private Date createExpireDate(Long tokenExpirationPeriod) {
        Date now = new Date();
        return new Date(now.getTime() + tokenExpirationPeriod);
    }

}
