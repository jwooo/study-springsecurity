package com.jwt.jwtexample.api.controller;

import com.jwt.jwtexample.api.request.UserSignUpDto;
import com.jwt.jwtexample.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public String signup(@RequestBody UserSignUpDto userSignUpDto) throws Exception{
        userService.signup(userSignUpDto);

        return "회원가입 완료";
    }

    @GetMapping("/jwt-test")
    public String jwtTest() {
        return "JWT-TEST 요청 성공";
    }
}
