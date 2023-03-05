package com.example.corespringsecurity;

import com.example.corespringsecurity.domain.Account;
import com.example.corespringsecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

@SpringBootApplication
@RequiredArgsConstructor
public class CorespringsecurityApplication {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(CorespringsecurityApplication.class, args);
    }

    @PostConstruct
    void init() {
        Account account = new Account("user", passwordEncoder.encode("1111"), "111", "20", "ROLE_USER");
        userRepository.save(account);
    }

}


