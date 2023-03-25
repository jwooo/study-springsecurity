package com.jwt.jwtexample.api.service;

import com.jwt.jwtexample.api.domain.User;
import com.jwt.jwtexample.api.repository.UserRepository;
import com.jwt.jwtexample.api.request.UserSignUpDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("중복되지 않은 email로 signup을 호출하면 정상적으로 회원이 저장된다")
    @Test
    public void not_duplicated_email_save_user() throws Exception{
        UserSignUpDto request = getUserSignUpDto();
        userService.signup(request);

        Optional<User> findUser = userRepository.findByEmail("aaa@aaa.com");

        assertThat(findUser).isNotEmpty();
        assertThat(findUser.get().getEmail()).isEqualTo(request.getEmail());
        assertThat(passwordEncoder.matches(request.getPassword(), findUser.get().getPassword())).isTrue();
    }

    @DisplayName("중복된 email로 signup을 호출하면 회원가입이 되지 않는다.")
    @Test
    public void duplicated_email_not_save_user() throws Exception {
        userService.signup(getUserSignUpDto());

        UserSignUpDto request = getUserSignUpDto();
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(Exception.class);
    }

    private static UserSignUpDto getUserSignUpDto() {
         return UserSignUpDto.builder()
                .email("aaa@aaa.com")
                .password("1234")
                .build();
    }
}