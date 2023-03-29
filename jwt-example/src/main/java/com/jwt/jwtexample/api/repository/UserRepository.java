package com.jwt.jwtexample.api.repository;

import com.jwt.jwtexample.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findByEmail(String email);
}
