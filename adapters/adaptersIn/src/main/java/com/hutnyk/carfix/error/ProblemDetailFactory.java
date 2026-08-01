package com.hutnyk.carfix.error;

import com.hutnyk.carfix.exception.CarFixException;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.Map;

/**
 * Builds every error body the API returns.
 * <p>
 * All five fields the web client's {@code isProblemDetailError} guard requires: {@code type},
 * {@code title}, {@code status}, {@code detail}, {@code instance} Spring's message converter fills from
 * the request path.
 */
@NoArgsConstructor
public final class ProblemDetailFactory {

    public static final String GENERIC_SERVER_DETAIL = "Something went wrong on our side. Please try again later.";

    public static ProblemDetail of(CarFixException exception) {
        HttpStatus status = ErrorStatusMapper.statusOf(exception.category());
        Map<String, String> details = exception.details();

        ProblemDetail problemDetail = of(status, ErrorStatusMapper.titleOf(exception.category()),
                detailFor(exception, status, details));
        problemDetail.setProperty("code", exception.getErrorCode().code());

        if (!details.isEmpty()) {
            problemDetail.setProperty("errors", details);
        }
        return problemDetail;
    }

    private static String detailFor(CarFixException exception, HttpStatus status, Map<String, String> details) {
        if (status.is5xxServerError()) {
            return GENERIC_SERVER_DETAIL;
        }
        return details.isEmpty()
                ? exception.getMessage()
                : ErrorStatusMapper.summaryOf(exception.category());
    }

    public static ProblemDetail of(HttpStatus status, String title, String detail, ErrorCode errorCode) {
        ProblemDetail problemDetail = of(status, title, detail);
        problemDetail.setProperty("code", errorCode.code());
        return problemDetail;
    }

    public static ProblemDetail of(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("about:blank"));
        return problemDetail;
    }
}
