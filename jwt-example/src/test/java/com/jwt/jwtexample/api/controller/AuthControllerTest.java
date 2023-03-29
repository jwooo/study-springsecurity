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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.MediaType.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
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

    // todo @ControllerAdvice 추가해서 Exception 통합 처리 예정
//    @DisplayName("회원가입시에 동일한 이메일 정보를 가진 유저가 있으면 Exception을 발생시킨다.")
//    @Test
//    public void sign_up_fail_same_user_in_db() throws Exception {
//        UserSignUpDto request = createUser();
//        userService.signup(request);
//
//        String json = objectMapper.writeValueAsString(request);
//        mockMvc.perform(post("/api/signup")
//                .contentType(APPLICATION_JSON)
//                .content(json))
//                .andExpect(status().isInternalServerError())
//                .andDo(print());
//    }

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

   // todo: @ControllerAdvice로 유효하지 않은 refreshToken을 반환하였을때 status Code 변경과 error Code return 해주기


    private static UserSignUpDto createUser() {
        return UserSignUpDto.builder()
                .email("aaa@aaa.com")
                .password("1234")
                .build();
    }
}