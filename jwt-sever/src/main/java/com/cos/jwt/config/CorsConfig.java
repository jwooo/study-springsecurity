package com.cos.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);   // 내 서버가 응답을 할 때 json을 자바 스크립트에서 처리할 수 있게 할지를 설정하는 것
        config.addAllowedOrigin("*");   // 모든 ip에 응답을 허용하겠다.
        config.addAllowedHeader("*");   // 모든 Header에 대한 응답을 허용하겠다.
        config.addAllowedMethod("*");   // 모든 post, get, patch, delete 메서드를 요청한다.
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
