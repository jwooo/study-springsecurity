package com.jwtoauth.jwtoauth.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtoauth.jwtoauth.global.jwt.filter.JwtAuthenticationProcessingFilter;
import com.jwtoauth.jwtoauth.global.jwt.service.JwtService;
import com.jwtoauth.jwtoauth.global.login.filter.CustomJsonUsernamePasswordFilter;
import com.jwtoauth.jwtoauth.global.login.handler.LoginFailureHandler;
import com.jwtoauth.jwtoauth.global.login.handler.LoginSuccessHandler;
import com.jwtoauth.jwtoauth.global.login.service.LoginService;
import com.jwtoauth.jwtoauth.global.oauth2.handler.OAuth2LoginFailureHandler;
import com.jwtoauth.jwtoauth.global.oauth2.handler.OAuth2LoginSuccessHandler;
import com.jwtoauth.jwtoauth.global.oauth2.service.CustomOAuth2UserService;
import com.jwtoauth.jwtoauth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginService loginService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .formLogin().disable()
                .httpBasic().disable()
                .csrf().disable()
                .headers().frameOptions().disable()
        .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
                .authorizeRequests()
                .antMatchers("/", "/css/**", "/images/**", "/js/**", "/favicon.ico", "/h2-console/**").permitAll()
                .antMatchers("/sign-up").permitAll()
                .anyRequest().authenticated()
        .and()
                .oauth2Login()
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
                .userInfoEndpoint().userService(customOAuth2UserService);

        http.addFilterAfter(customJsonUsernamePasswordFilter(), LogoutFilter.class);
        http.addFilterBefore(jwtAuthenticationProcessingFilter(), CustomJsonUsernamePasswordFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);

    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtService, userRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    @Bean
    public CustomJsonUsernamePasswordFilter customJsonUsernamePasswordFilter() {
        CustomJsonUsernamePasswordFilter customJsonUsernamePasswordFilter =
                new CustomJsonUsernamePasswordFilter(objectMapper);

        customJsonUsernamePasswordFilter.setAuthenticationManager(authenticationManager());
        customJsonUsernamePasswordFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customJsonUsernamePasswordFilter.setAuthenticationFailureHandler(loginFailureHandler());

        return customJsonUsernamePasswordFilter;
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, userRepository);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
