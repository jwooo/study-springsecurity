package com.example.corespringsecurity.controller.user;

import com.example.corespringsecurity.domain.Account;
import com.example.corespringsecurity.domain.AccountDto;
import com.example.corespringsecurity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final UserService userService;

    @GetMapping("/mypage")
    public String myPage() throws Exception {
        return "user/mypage";
    }

    @GetMapping("/users")
    public String createUser() {
        return "user/login/register";
    }

    @PostMapping("/users")
    public String createUser(AccountDto accountDto) {
        Account account = Account.builder()
                .username(accountDto.getUsername())
                .password(encodePassword(accountDto))
                .email(accountDto.getEmail())
                .age(accountDto.getAge())
                .role(accountDto.getRole())
                .build();
        userService.createUser(account);
        return "redirect:/";
    }

    private String encodePassword(AccountDto accountDto) {
        return passwordEncoder.encode(accountDto.getPassword());
    }
}
