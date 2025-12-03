package com.hutnyk.carfix.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    @SneakyThrows
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http
                .securityMatcher("/api/**")
                .authenticationProvider(authenticationProvider())
                .securityContext(context -> context.securityContextRepository(securityContextRepository()))
                .requestCache(RequestCacheConfigurer::disable) //disabling the request cache will prevent sessions from being created on failed authentication attempts
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )   .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/customer/auth/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Permits all to actuator endpoints, and denies all endpoints that are not listed in first <code>securityFilterChain</code>.
     */
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

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true);
            }
        };
    }

    @SneakyThrows
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }

    /**Configures <code>AuthenticationProvider</code> to {@link DaoAuthenticationProvider}.
     *
     * @return Bean
     */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    /**Configures <code>SecurityContextRepository</code> to {@link HttpSessionSecurityContextRepository}.
     * <p>Exposed for manual use in <code>login</code> controller, as starting from Spring Security 6.0 persistence of <code>HttpSession</code>
     * is handled manually.
     *
     * @return Bean
     */
    @Bean
    public SecurityContextRepository securityContextRepository(){
        return new HttpSessionSecurityContextRepository();
    }

    /**Configures <code>SessionAuthenticationStrategy</code> to {@link SessionFixationProtectionStrategy}.
     *
     * <p>Must be set manually starting from Spring Security 6.0, as SessionManagementFilter is no longer doing it automatically.
     *
     * <p>Enables protection against session fixation, must be called manually on login.
     *
     * @return Bean
     */
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(){
        return new SessionFixationProtectionStrategy();
    }
}
