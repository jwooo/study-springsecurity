package com.jwt.jwtexample.api.service;

import com.jwt.jwtexample.api.domain.User;
import com.jwt.jwtexample.api.repository.UserRepository;
import com.jwt.jwtexample.api.request.UserSignUpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jwt.jwtexample.api.domain.Role.USER;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserSignUpDto userSignUpDto) throws Exception {

        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("중복된 이메일을 입력하였습니다.");
        }

        User saveUser = User.builder()
                .email(userSignUpDto.getEmail())
                .password(passwordEncoder.encode(userSignUpDto.getPassword()))
                .role(USER)
                .build();
        userRepository.save(saveUser);
    }

}
