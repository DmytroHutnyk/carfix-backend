package com.hutnyk.carfix.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.exception.EmailAlreadyVerifiedException;
import com.hutnyk.carfix.user.exception.VerificationCodeInvalidException;
import com.hutnyk.carfix.user.exception.VerificationCodeResendTooSoonException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;

public class EmailVerificationControllerTest {

    private static final String EMAIL = "john@example.com";
    private static final Instant VERIFIED_AT = Instant.parse("2026-08-16T10:15:30Z");

    private static final class StubUserPortIn implements UserPortIn {
        String requestedFor;
        String verifiedFor;
        String receivedCode;
        RuntimeException requestFailure;
        RuntimeException verifyFailure;

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public User updateUser(String email, UpdateUserCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void requestEmailVerification(String email) {
            if (requestFailure != null) {
                throw requestFailure;
            }
            this.requestedFor = email;
        }

        @Override
        public User verifyEmail(String email, String code) {
            if (verifyFailure != null) {
                throw verifyFailure;
            }
            this.verifiedFor = email;
            this.receivedCode = code;
            return User.builder()
                    .id(UserId.genId())
                    .name("John")
                    .surname("Doe")
                    .phoneNumber(new PhoneNumber("+48", "123456789"))
                    .email(email)
                    .role(UserRole.CUSTOMER)
                    .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                    .dateOfBirth(null)
                    .addressId(null)
                    .emailVerifiedAt(VERIFIED_AT)
                    .build();
        }
    }

    private final StubUserPortIn stub = new StubUserPortIn();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new EmailVerificationController(stub))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(
                    Jackson2ObjectMapperBuilder.json()
                            .modules(new JavaTimeModule())
                            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .build()))
            .build();

    @BeforeEach
    public void authenticate() {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(EMAIL).password("x").roles("CUSTOMER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "x", principal.getAuthorities()));
    }

    @AfterEach
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void test_post_email_verification_returns_202_and_asks_the_port_for_the_principal() throws Exception {
        //when + then
        mockMvc.perform(post("/api/users/me/email-verification"))
                .andExpect(status().isAccepted());
        assertThat(stub.requestedFor).isEqualTo(EMAIL);
    }

    @Test
    public void test_post_email_verification_when_already_verified_returns_409_with_the_code() throws Exception {
        //given
        stub.requestFailure = new EmailAlreadyVerifiedException(EMAIL);
        //when + then
        mockMvc.perform(post("/api/users/me/email-verification"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_VERIFIED"))
                .andExpect(jsonPath("$.detail").value("Email john@example.com is already verified"));
    }

    @Test
    public void test_post_email_verification_inside_the_cooldown_returns_422() throws Exception {
        //given
        stub.requestFailure = new VerificationCodeResendTooSoonException(42);
        //when + then
        mockMvc.perform(post("/api/users/me/email-verification"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VERIFICATION_CODE_RESEND_TOO_SOON"));
    }

    @Test
    public void test_post_confirm_returns_200_with_the_verified_core() throws Exception {
        //when + then
        mockMvc.perform(post("/api/users/me/email-verification/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.emailVerifiedAt").value("2026-08-16T10:15:30Z"));
        assertThat(stub.verifiedFor).isEqualTo(EMAIL);
        assertThat(stub.receivedCode).isEqualTo("123456");
    }

    @Test
    public void test_post_confirm_with_a_malformed_code_returns_400_and_never_reaches_the_port() throws Exception {
        //when + then
        mockMvc.perform(post("/api/users/me/email-verification/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"12ab\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").exists());
        assertThat(stub.verifiedFor).isNull();
    }

    @Test
    public void test_post_confirm_with_a_wrong_code_returns_400_with_the_field_error() throws Exception {
        //given
        stub.verifyFailure = new VerificationCodeInvalidException(3);
        //when + then
        mockMvc.perform(post("/api/users/me/email-verification/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"123456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VERIFICATION_CODE_INVALID"))
                .andExpect(jsonPath("$.errors.code").value("Incorrect verification code. 3 attempt(s) left"));
    }
}
