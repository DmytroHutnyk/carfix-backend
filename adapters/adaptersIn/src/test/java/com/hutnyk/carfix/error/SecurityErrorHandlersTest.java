package com.hutnyk.carfix.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityErrorHandlersTest {

    /* Built through Jackson2ObjectMapperBuilder, not new ObjectMapper(): only the builder registers
     * ProblemDetailJacksonMixin, whose @JsonAnyGetter flattens the properties map so `code` lands at
     * the top level instead of nested under "properties". The ObjectMapper Spring Boot injects into
     * SecurityConfig at runtime carries the same mixin. */
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    public void test_the_entry_point_writes_a_parsable_401_body() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        new ProblemDetailAuthenticationEntryPoint(objectMapper).commence(
                request, response, new InsufficientAuthenticationException("no session"));

        //then
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/problem+json");

        var body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("type").asText()).isEqualTo("about:blank");
        assertThat(body.get("title").asText()).isEqualTo("Authentication failed");
        assertThat(body.get("status").asInt()).isEqualTo(401);
        assertThat(body.get("detail").asText()).isEqualTo("Authentication required");
        assertThat(body.get("instance").asText()).isEqualTo("/api/users/me");
        assertThat(body.get("code").asText()).isEqualTo("AUTHENTICATION_FAILED");
    }

    @Test
    public void test_the_access_denied_handler_writes_a_parsable_403_body() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/owner/branches");
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        new ProblemDetailAccessDeniedHandler(objectMapper).handle(
                request, response, new AccessDeniedException("denied"));

        //then
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/problem+json");

        var body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("title").asText()).isEqualTo("Access denied");
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("detail").asText()).isEqualTo("You are not allowed to perform this action");
        assertThat(body.get("instance").asText()).isEqualTo("/api/owner/branches");
        assertThat(body.get("code").asText()).isEqualTo("ACCESS_DENIED");
    }
}
