package com.jwt.jwtexample.api.security.jwt.filter;

import com.jwt.jwtexample.api.domain.User;
import com.jwt.jwtexample.api.repository.UserRepository;
import com.jwt.jwtexample.api.security.jwt.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    private static final String NO_CHECK_URL = "/api/login";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtProvider.extractAccessToken(request)
                .filter(jwtProvider::isValidToken)
                .orElse(null);

        if (isUnAuthenticationRequest(request, accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String subject = jwtProvider.getJwtTokenBody(accessToken).getSubject();
        User user = userRepository.findByEmail(subject)
                .orElseThrow(() -> new UsernameNotFoundException("일치하는 사용자를 찾을 수 없습니다."));
        authenticateUser(user);

        filterChain.doFilter(request, response);
    }

    private boolean isUnAuthenticationRequest(HttpServletRequest request, String accessToken) {
        return NO_CHECK_URL.equals(request.getRequestURI()) || accessToken == null;
    }

    private static void authenticateUser(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
