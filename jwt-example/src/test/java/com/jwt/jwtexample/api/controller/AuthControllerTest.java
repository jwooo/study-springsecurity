package com.jwt.jwtexample.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.jwtexample.api.domain.Role;
import com.jwt.jwtexample.api.domain.User;
import com.jwt.jwtexample.api.repository.UserRepository;
import com.jwt.jwtexample.api.request.UserSignUpDto;
import com.jwt.jwtexample.api.security.jwt.utils.JwtProvider;
import com.jwt.jwtexample.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("회원가입시에 동일한 이메일 정보를 가진 유저가 없으면 성공적으로 회원가입이 된다.")
    @Test
    public void sign_up_success_not_same_user_in_db() throws Exception {
        UserSignUpDto request = createUser();
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/signup")
                .contentType(APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("회원가입시에 동일한 이메일 정보를 가진 유저가 있으면 Exception을 발생시킨다.")
    @Test
    public void sign_up_fail_same_user_in_db() throws Exception {
        UserSignUpDto request = createUser();

        userService.signup(request);
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/signup")
                .contentType(APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일 입니다."))
                .andDo(print());
    }

   @DisplayName("요청 헤더에 Authorization-refresh 값을 넣고 전송하여 전송한 값이 유효하다면 새로운 Authorization-header들을 반환함")
   @Test
   public void send_Authorization_refresh_in_request_header_is_valid() throws Exception {
       String refreshToken = jwtProvider.createRefreshToken();
       userRepository.save(User.builder()
               .email("aaa@aaa.com")
               .password(passwordEncoder.encode("1234"))
               .role(Role.USER)
               .refreshToken(refreshToken)
               .build());

       mockMvc.perform(post("/api/refresh")
               .header("Authorization-refresh", "Bearer " + refreshToken))
               .andExpect(status().isOk())
               .andExpect(header().exists("Authorization"))
               .andExpect(header().exists("Authorization-refresh"))
               .andDo(print());
   }

    @DisplayName("요청 헤더에 유효하지 않은 refresh-token을 전송한다.")
    @Test
    public void send_authorization_refresh_in_request_header_is_invalid() throws Exception {
        String refreshToken = jwtProvider.createRefreshToken();
        String invalidToken = "Bearer " + refreshToken + "is invalid";

        userRepository.save(User.builder()
                .email("aaa@aaa.com")
                .password(passwordEncoder.encode("1234"))
                .role(Role.USER)
                .refreshToken(refreshToken)
                .build());

        mockMvc.perform(post("/api/refresh")
                .header("Authorization-refresh", invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("허용되지 않는 리프레쉬 토큰입니다."))
                .andDo(print());
    }

    @DisplayName("발급한 적 없는 refresh-token을 전송한다.")
    @Test
    public void send_authorization_refresh_not_reissue_token() throws Exception {
        String refreshToken = jwtProvider.createRefreshToken();

        mockMvc.perform(post("/api/refresh")
                .header("Authorization-refresh", refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("허용되지 않는 리프레쉬 토큰입니다."))
                .andDo(print());
    }



    private static UserSignUpDto createUser() {
        return UserSignUpDto.builder()
                .email("aaa@aaa.com")
                .password("1234")
                .build();
    }
}