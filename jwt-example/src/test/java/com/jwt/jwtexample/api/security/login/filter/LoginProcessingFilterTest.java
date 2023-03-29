package com.jwt.jwtexample.api.security.login.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.jwtexample.api.domain.Role;
import com.jwt.jwtexample.api.domain.User;
import com.jwt.jwtexample.api.repository.UserRepository;
import com.jwt.jwtexample.api.request.UserLoginDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class LoginProcessingFilterTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private LoginProcessingFilter loginProcessingFilter;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;


    private MockMvc mockMvc;


    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(loginProcessingFilter)
                .build();

        userRepository.save(User.builder()
                .email("aaa@naver.com")
                .password(passwordEncoder.encode("1234"))
                .role(Role.USER)
                .build());
    }

    @DisplayName("회원가입이 되어 있는 정보와 응답 BODY에 있는 값이 일치하면 access Token과 refresh Token을 응답 헤더에 발급한다.")
    @Test
    public void success_issued_access_token_refresh_token_in_response_header() throws Exception {
        UserLoginDto request = UserLoginDto.builder()
                .email("aaa@naver.com")
                .password("1234")
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/login")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(header().exists("Authorization-refresh"))
                .andDo(print());
    }

    @DisplayName("이메일이 일치하지 않으면 400 status code 발생")
    @Test
    public void not_match_email_cause_400_status_code() throws Exception {
        UserLoginDto request = UserLoginDto.builder()
                .email("aab@naver.com")
                .password("1234")
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/login")
                        .contentType(APPLICATION_JSON)
                        .content((json)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("이메일은 일치하지만 비밀번호가 일치하지 않으면 400 status code 발생")
    @Test
    public void match_email_not_match_password_cause_400_status_code() throws Exception {
        UserLoginDto request = UserLoginDto.builder()
                .email("aaa@naver.com")
                .password("1111")
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/login")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("Request Body에 null 값을 저장하고 전송하면 400 status code 발생")
    @Test
    public void null_in_request_body_cause_400_status_code() throws Exception {

        mockMvc.perform(post("/api/login")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("Content-Type을 application/json으로 전송하지 않으면 400 status code를 발생시킨다.")
    @Test
    public void content_type_not_application_json_cause_400_status_code() throws Exception {
        UserLoginDto request = UserLoginDto.builder()
                .email("aaa@naver.com")
                .password("1234")
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/login")
                .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}