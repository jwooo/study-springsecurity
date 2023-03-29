package com.jwt.jwtexample.api.controller;

import com.jwt.jwtexample.api.request.UserSignUpDto;
import com.jwt.jwtexample.api.security.jwt.utils.JwtProvider;
import com.jwt.jwtexample.api.security.jwt.utils.TokenDto;
import com.jwt.jwtexample.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public String signup(@RequestBody UserSignUpDto userSignUpDto) throws Exception{
        userService.signup(userSignUpDto);

        return "회원가입 완료";
    }

    @PostMapping("/refresh")
    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.extractRefreshToken(request)
                .filter(jwtProvider::isValidToken)
                .orElseThrow(RuntimeException::new);
        TokenDto tokenDto = userService.reIssueToken(refreshToken);

        response.setHeader(jwtProvider.getAccessHeader(), tokenDto.getAccessToken());
        response.setHeader(jwtProvider.getRefreshHeader(), tokenDto.getRefreshToken());
    }

    @GetMapping("/jwt-test")
    public String jwtTest() {
        return "JWT-TEST 요청 성공";
    }
}
