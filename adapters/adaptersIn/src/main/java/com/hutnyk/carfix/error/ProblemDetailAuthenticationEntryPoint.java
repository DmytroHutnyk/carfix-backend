package com.hutnyk.carfix.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutnyk.carfix.exception.CoreErrorCode;
import com.hutnyk.carfix.exception.ErrorCategory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Answers unauthenticated requests with the same problem body the rest of the API uses, replacing
 * {@code HttpStatusEntryPoint}, which returns a 401 with no body at all.
 * <p>
 * The detail never says whether the account exists or the session merely expired.
 */
@RequiredArgsConstructor
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ProblemDetailWriter.write(objectMapper, request, response, ProblemDetailFactory.of(
                HttpStatus.UNAUTHORIZED,
                ErrorStatusMapper.titleOf(ErrorCategory.AUTHENTICATION),
                "Authentication required",
                CoreErrorCode.AUTHENTICATION_FAILED));
    }
}
