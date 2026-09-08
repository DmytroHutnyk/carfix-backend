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

/** Gives filter-chain 401s the same body as controller failures without leaking account existence. */
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
