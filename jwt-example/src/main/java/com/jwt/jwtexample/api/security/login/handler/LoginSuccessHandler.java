package com.jwt.jwtexample.api.security.login.handler;

import com.jwt.jwtexample.api.domain.User;
import com.jwt.jwtexample.api.repository.UserRepository;
import com.jwt.jwtexample.api.security.jwt.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String accessToken = jwtProvider.createAccessToken(extractEmail(authentication));
        String refreshToken = jwtProvider.createRefreshToken();

        jwtProvider.sendAccessTokenAndRefreshToken(response, accessToken, refreshToken);

        userRepository.findByEmail(extractEmail(authentication))
                .ifPresent(user -> {
                    user.updateRefreshToken(refreshToken);
                    userRepository.saveAndFlush(user);
                });
    }

    private String extractEmail(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
