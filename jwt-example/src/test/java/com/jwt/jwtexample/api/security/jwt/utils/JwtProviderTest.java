package com.jwt.jwtexample.api.security.jwt.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Optional;

import static io.jsonwebtoken.SignatureAlgorithm.HS512;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;


    @Test
    void test() {
    }
}