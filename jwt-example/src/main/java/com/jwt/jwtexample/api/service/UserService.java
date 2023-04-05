package com.jwt.jwtexample.api.service;

import com.jwt.jwtexample.api.domain.User;
import com.jwt.jwtexample.api.exception.AlreadyExistsEmailException;
import com.jwt.jwtexample.api.exception.NotAllowedRefreshToken;
import com.jwt.jwtexample.api.repository.UserRepository;
import com.jwt.jwtexample.api.request.UserSignUpDto;
import com.jwt.jwtexample.api.security.jwt.utils.JwtProvider;
import com.jwt.jwtexample.api.security.jwt.utils.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.jwt.jwtexample.api.domain.Role.USER;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserSignUpDto userSignUpDto) {

        Optional<User> findOptionalUser = userRepository.findByEmail(userSignUpDto.getEmail());

        if (findOptionalUser.isPresent()) {
            throw new AlreadyExistsEmailException();
        }

        User saveUser = User.builder()
                .email(userSignUpDto.getEmail())
                .password(passwordEncoder.encode(userSignUpDto.getPassword()))
                .role(USER)
                .build();
        userRepository.save(saveUser);
    }

    public TokenDto reIssueToken(String refreshToken) {
        User findUser = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(NotAllowedRefreshToken::new);
        TokenDto tokenDto = jwtProvider.createTokenDto(findUser);
        findUser.updateRefreshToken(tokenDto.getRefreshToken());

        return tokenDto;
    }

}
