package com.hutnyk.carfix.error;

import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import com.hutnyk.carfix.user.exception.EmailAlreadyTakenException;
import com.hutnyk.carfix.user.exception.PhoneNumberAlreadyTakenException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

    @RestController
    public static class ThrowingController {

        public record Payload(String name) {}

        @GetMapping("/probe/validation")
        public String validation() {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_VIN_FORMAT, "vin", "ABC");
        }

        @GetMapping("/probe/not-found")
        public String notFound() {
            throw new CarProfileNotFoundException(UUID.fromString("00000000-0000-0000-0000-000000000007"));
        }

        @GetMapping("/probe/conflict-email")
        public String conflictEmail() {
            throw new EmailAlreadyTakenException("john@example.com");
        }

        @GetMapping("/probe/conflict-phone")
        public String conflictPhone() {
            throw new PhoneNumberAlreadyTakenException(new PhoneNumber("+48", "123456789"));
        }

        @GetMapping("/probe/unauthenticated")
        public String unauthenticated() {
            throw AuthenticatedUserMissingException.forEmail("john@example.com");
        }

        @GetMapping("/probe/internal")
        public String internal() {
            throw new UnexpectedStateException("row vanished mid-transaction");
        }

        @GetMapping("/probe/unmapped")
        public String unmapped() {
            throw new IllegalArgumentException("a leaky internal detail");
        }

        @PostMapping("/probe/body")
        public String body(@RequestBody Payload payload) {
            return payload.name();
        }
    }

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    public void test_a_validation_failure_is_400_with_the_field_under_errors() throws Exception {
        //when + then
        mockMvc.perform(get("/probe/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/probe/validation"))
                .andExpect(jsonPath("$.code").value("INVALID_VIN_FORMAT"))
                .andExpect(jsonPath("$.errors.vin").value("vin: VIN format is not valid (received: ABC)"))
                //detail summarises rather than repeating the sentence already in errors —
                //the web client concatenates the two, and would otherwise print it twice
                .andExpect(jsonPath("$.detail").value("Validation failed"));
    }

    @Test
    public void test_every_error_body_carries_the_five_fields_the_web_client_requires() throws Exception {
        //then — isProblemDetailError() in the web client rejects a body missing any of these,
        //and silently drops the errors map with it
        for (String path : new String[]{"/probe/validation", "/probe/not-found", "/probe/conflict-email",
                "/probe/unauthenticated", "/probe/internal", "/probe/unmapped"}) {
            mockMvc.perform(get(path))
                    .andExpect(jsonPath("$.type").isString())
                    .andExpect(jsonPath("$.title").isString())
                    .andExpect(jsonPath("$.status").isNumber())
                    .andExpect(jsonPath("$.detail").isString())
                    .andExpect(jsonPath("$.instance").isString())
                    .andExpect(jsonPath("$.code").isString());
        }
    }

    @Test
    public void test_a_missing_resource_is_404() throws Exception {
        //when + then
        mockMvc.perform(get("/probe/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"))
                .andExpect(jsonPath("$.detail").value("Car profile not found: 00000000-0000-0000-0000-000000000007"))
                .andExpect(jsonPath("$.code").value("CAR_PROFILE_NOT_FOUND"));
    }

    @Test
    public void test_a_taken_email_is_409_keyed_on_the_form_field() throws Exception {
        //when + then
        mockMvc.perform(get("/probe/conflict-email"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_TAKEN"))
                .andExpect(jsonPath("$.detail").value("Request conflicts with existing data"))
                .andExpect(jsonPath("$.errors.email").value("User with john@example.com email already exists"));
    }

    @Test
    public void test_a_taken_phone_number_is_409_keyed_on_the_form_field() throws Exception {
        //when + then
        mockMvc.perform(get("/probe/conflict-phone"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.phoneCountryCodeAndPhoneNumber")
                        .value("User with 123456789 phone number already exists"));
    }

    @Test
    public void test_a_dead_session_is_401() throws Exception {
        //when + then
        mockMvc.perform(get("/probe/unauthenticated"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication failed"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATED_USER_MISSING"));
    }

    @Test
    public void test_a_server_side_failure_never_leaks_its_message() throws Exception {
        //when + then
        mockMvc.perform(get("/probe/internal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value("Something went wrong on our side. Please try again later."));
    }

    @Test
    public void test_an_unmapped_exception_is_a_generic_500() throws Exception {
        //when + then
        mockMvc.perform(get("/probe/unmapped"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value("Something went wrong on our side. Please try again later."))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("leaky"))));
    }

    @Test
    public void test_unreadable_json_is_a_parsable_400() throws Exception {
        //when + then — the framework builds this body; handleExceptionInternal patches in the code
        mockMvc.perform(post("/probe/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Failed to read request"))
                .andExpect(jsonPath("$.instance").value("/probe/body"))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    public void test_the_wrong_verb_is_a_parsable_405() throws Exception {
        //when + then
        mockMvc.perform(get("/probe/body"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Method Not Allowed"))
                .andExpect(jsonPath("$.detail").value("Method 'GET' is not supported."))
                .andExpect(jsonPath("$.instance").value("/probe/body"))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }
}
