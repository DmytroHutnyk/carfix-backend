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

/** Filter-chain responses bypass message converters, so this also fills required instance URI. */
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
