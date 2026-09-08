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

/** Makes filter-chain and method-level authorization failures indistinguishable. */
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
