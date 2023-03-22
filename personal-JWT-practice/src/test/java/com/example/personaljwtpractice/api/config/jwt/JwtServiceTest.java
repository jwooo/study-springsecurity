package com.example.personaljwtpractice.api.config.jwt;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class JwtServiceTest {

    @Autowired
    JwtService jwtService;

    String email = "aaa@naver.com";


    @Test
    @DisplayName("Authorization 헤더를 넘기면 Bearer을 제거한 accessToken을 리턴한다.")
    public void extractAccessToken_request_include_Bearer() {
        String accessToken = jwtService.createAccessToken(email);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", addBearer(accessToken));

        Optional<String> resultAccessToken = jwtService.extractAccessToken(request);
        assertThat(resultAccessToken).isNotEmpty();
    }

    @Test
    @DisplayName("Authorization 헤더에 Bearer이 없으면 null을 리턴한다.")
    public void extractAccessToken_request_not_include_Bearer() {
        String accessToken = jwtService.createAccessToken(email);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", accessToken);

        Optional<String> resultAccessToken = jwtService.extractAccessToken(request);
        assertThat(resultAccessToken).isEmpty();
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 null을 리턴한다.")
    public void extractAccessToken_not_requestHeader() {
        String accessToken = jwtService.createAccessToken(email);
        MockHttpServletRequest request = new MockHttpServletRequest();

        Optional<String> resultAccessToken = jwtService.extractAccessToken(request);
        assertThat(resultAccessToken).isEmpty();
    }

    @Test
    @DisplayName("Authorization_refresh 헤더를 넘기면 Bearer를 제거한 refreshToken을 리턴한다.")
    public void extractRefreshToken_request_include_Bearer() {
        String refreshToken = jwtService.createRefreshToken();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization-refresh", addBearer(refreshToken));

        Optional<String> resultRefreshToken = jwtService.extractRefreshToken(request);
        assertThat(resultRefreshToken).isNotEmpty();
    }

    @Test
    @DisplayName("Authorization_refresh 헤더에 Bearer이 없으면 null을 리턴한다.")
    public void extractRefreshToken_request_not_include_Bearer() {
        String refreshToken = jwtService.createRefreshToken();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization-refresh", refreshToken);

        Optional<String> resultRefreshToken = jwtService.extractRefreshToken(request);
        assertThat(resultRefreshToken).isEmpty();
    }

    @Test
    @DisplayName("Authorization-refresh 헤더가 없으면 null을 리턴한다.")
    public void extractRefreshToken_not_requestHeader() {
        String refreshToken = jwtService.createRefreshToken();
        MockHttpServletRequest request = new MockHttpServletRequest();

        Optional<String> resultRefreshToken = jwtService.extractRefreshToken(request);
        assertThat(resultRefreshToken).isEmpty();
    }

    @Test
    @DisplayName("AccessToken의 값이 수정되지 않았으면 true를 리턴한다.")
    public void not_modify_accessToken_value_return_true() {
        String accessToken = jwtService.createAccessToken(email);

        assertThat(jwtService.isTokenValid(accessToken)).isTrue();
    }

    @Test
    @DisplayName("AccessToken의 값이 수정되었으면 false를 리턴한다.")
    public void modify_accessToken_value_return_false() {
        String accessToken = jwtService.createAccessToken(email);
        String modifyAccessToken = accessToken + "random_string";
        assertThat(jwtService.isTokenValid(modifyAccessToken)).isFalse();
    }

    @Test
    @DisplayName("RefreshToken의 값이 수정되지 않았으면 true를 리턴한다.")
    public void not_modify_refreshToken_value_return_true() {
        String refreshToken = jwtService.createRefreshToken();
        assertThat(jwtService.isTokenValid(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("RefreshToken의 값이 수정되었으면 false를 리턴한다.")
    public void modify_refreshToken_value_return_false() {
        String refreshToken = jwtService.createRefreshToken();
        String modifyRefreshToken = refreshToken + "random_string";
        assertThat(jwtService.isTokenValid(modifyRefreshToken)).isFalse();
    }

    private String addBearer(String token) {
        return "Bearer " + token;
    }
}