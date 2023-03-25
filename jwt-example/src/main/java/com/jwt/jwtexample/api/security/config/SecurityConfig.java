package com.jwt.jwtexample.api.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.jwtexample.api.security.login.filter.LoginProcessingFilter;
import com.jwt.jwtexample.api.security.login.service.LoginService;
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

    private final ObjectMapper objectMapper;

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
                .mvcMatchers("/h2-console/**", "/").permitAll()
                .mvcMatchers("/api/signup").permitAll()
                .anyRequest().authenticated();

        http.addFilterAfter(loginProcessingFilter(), LogoutFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);

        return new ProviderManager(provider);
    }


    @Bean
    public LoginProcessingFilter loginProcessingFilter() {
        LoginProcessingFilter filter = new LoginProcessingFilter(objectMapper);

        filter.setAuthenticationManager(authenticationManager());
//        filter.setAuthenticationSuccessHandler(null);
//        filter.setAuthenticationFailureHandler(null);

        return filter;
    }
}
