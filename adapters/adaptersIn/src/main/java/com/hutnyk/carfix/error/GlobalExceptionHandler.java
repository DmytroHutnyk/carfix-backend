package com.hutnyk.carfix.error;

import com.hutnyk.carfix.exception.CarFixException;
import com.hutnyk.carfix.exception.CoreErrorCode;
import com.hutnyk.carfix.exception.ErrorCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Turns every exception that escapes a controller into an RFC-7807 problem body.
 * <p>
 * There is one handler for the whole {@link CarFixException} hierarchy.
 * The remaining handlers exist only for exceptions we do not own: Spring Security's and the
 * framework's own request-level failures inherited from {@link ResponseEntityExceptionHandler}
 * (unsupported method, unreadable JSON, missing parameter), which would otherwise fall through
 * to Spring's default {@code /error} body that the web client cannot parse.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Every failure the application throws on purpose.
     *
     * @return {@code ResponseEntity<ProblemDetail>} whose status comes from the exception's category
     */
    @ExceptionHandler(CarFixException.class)
    public ResponseEntity<ProblemDetail> handleCarFix(CarFixException exception) {
        ProblemDetail problemDetail = ProblemDetailFactory.of(exception);
        logByStatus(exception, problemDetail.getStatus(), exception.getErrorCode().code());
        return ResponseEntity.status(problemDetail.getStatus()).body(problemDetail);
    }

    /**
     * Wrong email or password.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException exception) {
        ProblemDetail problemDetail = ProblemDetailFactory.of(
                HttpStatus.UNAUTHORIZED,
                ErrorStatusMapper.titleOf(ErrorCategory.AUTHENTICATION),
                "Wrong email or password",
                CoreErrorCode.AUTHENTICATION_FAILED);
        log.debug("Login rejected: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    /**
     * Thrown by {@code @PreAuthorize} inside the dispatch. URL-rule denials never reach here,
     * they are raised in the filter chain and answered by {@code ProblemDetailAccessDeniedHandler}.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException exception) {
        ProblemDetail problemDetail = ProblemDetailFactory.of(
                HttpStatus.FORBIDDEN,
                ErrorStatusMapper.titleOf(ErrorCategory.AUTHORIZATION),
                "You are not allowed to perform this action",
                CoreErrorCode.ACCESS_DENIED);
        log.debug("Access denied: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    /**
     * Anything we failed to anticipate. The real message is logged, never returned.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
        ProblemDetail problemDetail = ProblemDetailFactory.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorStatusMapper.titleOf(ErrorCategory.INTERNAL),
                ProblemDetailFactory.GENERIC_SERVER_DETAIL,
                CoreErrorCode.INTERNAL_ERROR);
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /**
     * Bean-validation failures on request DTOs. Reports every field at once under {@code errors}.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problemDetail = ProblemDetailFactory.of(
                HttpStatus.BAD_REQUEST,
                ErrorStatusMapper.titleOf(ErrorCategory.VALIDATION),
                "Validation failed for request DTO",
                CoreErrorCode.VALIDATION_FAILED);

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError fieldError
                    ? fieldError.getField()
                    : error.getObjectName();
            errors.put(fieldName, error.getDefaultMessage());
        });
        problemDetail.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * Fills in what was not explicitly, so their bodies satisfy the same contract.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(exception, body, headers, statusCode, request);

        if (response != null && response.getBody() instanceof ProblemDetail problemDetail) {
            HttpStatus status = HttpStatus.valueOf(statusCode.value());
            if (problemDetail.getDetail() == null) {
                problemDetail.setDetail(status.getReasonPhrase());
            }
            if (problemDetail.getType() == null) {
                problemDetail.setType(URI.create("about:blank"));
            }
            problemDetail.setProperty("code", status.is5xxServerError()
                    ? CoreErrorCode.INTERNAL_ERROR.code()
                    : CoreErrorCode.MALFORMED_REQUEST.code());
        }
        return response;
    }

    private void logByStatus(CarFixException exception, int status, String code) {
        if (status >= 500) {
            log.error("[{}] {}", code, exception.getMessage(), exception);
        } else if (exception.category() == ErrorCategory.AUTHENTICATION) {
            log.warn("[{}] {}", code, exception.getMessage());
        } else {
            log.debug("[{}] {}", code, exception.getMessage());
        }
    }
}
