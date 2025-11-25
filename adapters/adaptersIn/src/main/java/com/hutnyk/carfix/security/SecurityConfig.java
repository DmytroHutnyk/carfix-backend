package com.hutnyk.carfix.security;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    @Order(1)
    @SneakyThrows
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/api/auth/login")
                        .loginProcessingUrl("/api/auth/login")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/api/auth/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    @SneakyThrows
    public SecurityFilterChain defaultFilterChain(HttpSecurity http){
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info")
                        .permitAll()
                        .anyRequest()
                        .denyAll()
                );

        return http.build();
    }
}
