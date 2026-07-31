package com.hutnyk.carfix.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutnyk.carfix.exception.CoreErrorCode;
import com.hutnyk.carfix.exception.ErrorCategory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Answers URL-rule authorization failures raised in the filter chain, which never reach
 * {@code GlobalExceptionHandler}. Produces the same body as the {@code @PreAuthorize} path so a
 * client cannot tell the two apart.
 */
@RequiredArgsConstructor
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ProblemDetailWriter.write(objectMapper, request, response, ProblemDetailFactory.of(
                HttpStatus.FORBIDDEN,
                ErrorStatusMapper.titleOf(ErrorCategory.AUTHORIZATION),
                "You are not allowed to perform this action",
                CoreErrorCode.ACCESS_DENIED));
    }
}
