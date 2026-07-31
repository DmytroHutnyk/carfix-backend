package com.hutnyk.carfix.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Serializes a {@link ProblemDetail} straight onto the servlet response.
 * <p>
 * Needed because Spring Security answers from inside the filter chain, where no message converter
 * runs, which also means {@code instance} is not filled in for us, so it is set here from the
 * request URI. Without it the web client's {@code isProblemDetailError} guard rejects the body.
 */
@NoArgsConstructor
final class ProblemDetailWriter {
    public static void write(ObjectMapper objectMapper,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             ProblemDetail problemDetail) throws IOException {
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(problemDetail.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
