package com.jwtoauth.jwtoauth.global.jwt.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtoauth.jwtoauth.global.jwt.service.JwtService;
import com.jwtoauth.jwtoauth.user.entity.Role;
import com.jwtoauth.jwtoauth.user.entity.User;
import com.jwtoauth.jwtoauth.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtAuthenticationProcessingFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Autowired
    JwtService jwtService;

    @Autowired
    ObjectMapper objectMapper;

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String EMAIL = "test1@naver.com";
    private static final String PASSWORD = "password1";
    private static final String LOGIN_URL = "/login";
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String BEARER = "Bearer ";

    @BeforeEach
    private void init() {
        userRepository.save(User.builder()
                .email(EMAIL)
                .password(delegatingPasswordEncoder.encode(PASSWORD))
                .nickname("PJW")
                .role(Role.USER)
                .age(24)
                .city("seoul")
                .build());
        clear();
    }

    private void clear() {
        em.flush();
        em.clear();
    }

    private Map<String, String> getUsernamePasswordMap(String email, String password) {
        Map<String, String> usernamePasswordMap = new LinkedHashMap<>();
        usernamePasswordMap.put(KEY_EMAIL, email);
        usernamePasswordMap.put(KEY_PASSWORD, password);
        return usernamePasswordMap;
    }

    private Map<String, String> getTokenMap() throws Exception {
        Map<String, String> usernamePasswordMap = getUsernamePasswordMap(EMAIL, PASSWORD);

        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usernamePasswordMap)))
                .andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader);
        String refreshToken = result.getResponse().getHeader(refreshHeader);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(accessHeader, accessToken);
        tokenMap.put(refreshHeader, refreshToken);
        return tokenMap;
    }

    @Test
    @DisplayName("AccessToken, RefreshToken 모두 존재하지 않는 경우 - /login로 302 리다이렉트")
    void Access_Refresh_not_exist() throws Exception {
        mockMvc.perform(get("/jwt-test"))
                .andExpect(status().isFound())
                .andDo(print());
    }

    @Test
    @DisplayName("유효한 AccessToken만 요청 - 인증 성공 200")
    void Access_valid_request() throws Exception {
        Map<String, String> tokenMap = getTokenMap();
        String accessToken = tokenMap.get(accessHeader);

        mockMvc.perform(get("/jwt-test")
                .header(accessHeader, BEARER + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유효하지 않는 AccessToken만 요청 - /login으로 302 리다이렉트")
    void Access_not_valid_request() throws Exception {
        Map<String, String> tokenMap = getTokenMap();
        String accessToken = tokenMap.get(accessHeader);

        mockMvc.perform(get("/jwt-test")
                .header(accessHeader, BEARER + accessToken + "1"))
                .andExpect(status().isFound());
    }

    @Test
    @DisplayName("AccessToken 만료된 경우, RefreshToken 유효한 경우 - AccessToken/RefreshToken 재발급 후 200")
    void Access_expired_and_refresh_valid() throws Exception {
        Map<String, String> tokenMap = getTokenMap();
        String refreshToken = tokenMap.get(refreshHeader);

        MvcResult result = mockMvc.perform(get("/jwt-test")
                .header(refreshHeader, BEARER + refreshToken))
                .andExpect(status().isOk()).andReturn();
        String accessToken = result.getResponse().getHeader(accessHeader);
        String reIssuedRefreshToken = result.getResponse().getHeader(refreshHeader);

        String accessTokenSubject = JWT.require(Algorithm.HMAC512(secretKey)).build()
                .verify(accessToken).getSubject();
        System.out.println("accessTokenSubject = " + accessTokenSubject);
        String refreshTokenSubject = JWT.require(Algorithm.HMAC512(secretKey)).build()
                .verify(reIssuedRefreshToken).getSubject();
        System.out.println("refreshTokenSubject = " + refreshTokenSubject);

        assertThat(accessTokenSubject).isEqualTo(ACCESS_TOKEN_SUBJECT);
        assertThat(refreshTokenSubject).isEqualTo(REFRESH_TOKEN_SUBJECT);
    }

    @Test
    @DisplayName("AccessToken 만료된 경우, RefreshToken 유효하지 않은 경우 /login로 302 리다이렉트")
    void Access_expired_and_Refresh_not_valid() throws Exception {
        Map<String, String> tokenMap = getTokenMap();
        String refreshToken = tokenMap.get(refreshHeader);

        mockMvc.perform(get("/jwt-test")
                .header(refreshHeader, BEARER + refreshToken + "1"))
                .andExpect(status().isFound());
    }

    @Test
    @DisplayName("POST /login으로 요청 보내면 JWT 필터 작동 X")
    void Login_URL_not_run_jwt_filter() throws Exception {
        mockMvc.perform(post("/login"))
                .andExpect(status().isBadRequest());
    }
}