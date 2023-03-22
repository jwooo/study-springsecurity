package com.example.personaljwtpractice.api.config;

import com.example.personaljwtpractice.api.config.auth.LoginAuthenticationAccessDeniedHandler;
import com.example.personaljwtpractice.api.config.auth.LoginAuthenticationEntryPoint;
import com.example.personaljwtpractice.api.config.jwt.filter.JwtAuthenticationProcessingFilter;
import com.example.personaljwtpractice.api.config.jwt.JwtService;
import com.example.personaljwtpractice.api.config.login.LoginService;
import com.example.personaljwtpractice.api.config.login.filter.CustomJsonUsernamePasswordFilter;
import com.example.personaljwtpractice.api.config.login.handler.LoginFailureHandler;
import com.example.personaljwtpractice.api.config.login.handler.LoginSuccessHandler;
import com.example.personaljwtpractice.api.config.oauth2.handler.OAuth2LoginFailureHandler;
import com.example.personaljwtpractice.api.config.oauth2.handler.OAuth2LoginSuccessHandler;
import com.example.personaljwtpractice.api.config.oauth2.service.CustomOAuth2UserService;
import com.example.personaljwtpractice.api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                .exceptionHandling()
                .authenticationEntryPoint(loginAuthenticationEntryPoint())
                .accessDeniedHandler(loginAuthenticationAccessDeniedHandler())
        .and()
                .oauth2Login()
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                        .userInfoEndpoint()
                            .userService(customOAuth2UserService);

        http.addFilterAfter(customJsonUsernamePasswordFilter(), LogoutFilter.class);
        http.addFilterBefore(jwtAuthenticationProcessingFilter(), LogoutFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, userRepository);
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
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtService, userRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler(objectMapper);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    @Bean
    public LoginAuthenticationEntryPoint loginAuthenticationEntryPoint() {
        return new LoginAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public LoginAuthenticationAccessDeniedHandler loginAuthenticationAccessDeniedHandler() {
        return new LoginAuthenticationAccessDeniedHandler(objectMapper);
    }
}
