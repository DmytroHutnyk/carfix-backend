package com.hutnyk.carfix.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hutnyk.carfix.error.GlobalExceptionHandler;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.in.user.commands.UpdateUserAddressCommand;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
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

import java.time.LocalDate;
import java.util.Optional;

public class UserControllerTest {

    private static final String EMAIL = "john@example.com";

    private static final class StubUserPortIn implements UserPortIn {
        UpdateUserCommand received;
        String receivedEmail;
        int calls;

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public User updateUser(String email, UpdateUserCommand command) {
            this.receivedEmail = email;
            this.received = command;
            this.calls++;
            /* Built through the builder, not the 9-arg User.of(...), so that adding an optional
             * domain field does not break this file. If a new *required* field lands, these tests
             * fail with a DomainObjectValidationException naming it — add it here. */
            return User.builder()
                    .id(UserId.genId())
                    .name(command.name())
                    .surname(command.surname())
                    .phoneNumber(new PhoneNumber("+48", "123456789"))
                    .email(email)
                    .role(UserRole.CUSTOMER)
                    .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                    .dateOfBirth(command.dateOfBirth())
                    .addressId(null)
                    .preferredLocation(null)
                    .build();
        }

        @Override
        public AddressView updateAddress(String email, UpdateUserAddressCommand command) {
            throw new UnsupportedOperationException("wired in Task 5");
        }

        @Override
        public void deleteAddress(String email) {
            throw new UnsupportedOperationException("wired in Task 5");
        }
    }

    private final StubUserPortIn stub = new StubUserPortIn();

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new UserController(stub))
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
    public void test_put_me_returns_200_with_the_updated_core() throws Exception {
        //when + then
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":\"1990-05-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.phoneCountryCode").value("+48"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-05-01"));
    }

    @Test
    public void test_put_me_passes_the_principal_email_and_command_fields_verbatim() throws Exception {
        //when
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":\"1990-05-01\"}"))
                .andExpect(status().isOk());

        //then — asserted field-wise, not as whole-record equality, so that adding a component to
        //UpdateUserCommand does not break this file
        assertThat(stub.receivedEmail).isEqualTo(EMAIL);
        assertThat(stub.received.name()).isEqualTo("John");
        assertThat(stub.received.surname()).isEqualTo("Doe");
        assertThat(stub.received.dateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 1));
    }

    @Test
    public void test_put_me_null_dateOfBirth_reaches_the_port_as_null() throws Exception {
        //when
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateOfBirth").doesNotExist());

        //then
        assertThat(stub.received.dateOfBirth()).isNull();
    }

    @Test
    public void test_put_me_blank_name_returns_400_and_never_reaches_the_port() throws Exception {
        //when + then
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \",\"surname\":\"Doe\",\"dateOfBirth\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        assertThat(stub.calls).isZero();
    }

    @Test
    public void test_put_me_absent_surname_returns_400() throws Exception {
        //when + then
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"dateOfBirth\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.surname").exists());

        assertThat(stub.calls).isZero();
    }

    @Test
    public void test_patch_me_is_no_longer_mapped() throws Exception {
        //when + then
        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"surname\":\"Doe\",\"dateOfBirth\":null}"))
                .andExpect(status().isMethodNotAllowed());
    }
}
