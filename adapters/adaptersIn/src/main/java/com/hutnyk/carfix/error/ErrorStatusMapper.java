package com.hutnyk.carfix.error;

import com.hutnyk.carfix.exception.ErrorCategory;
import org.springframework.http.HttpStatus;

/**
 * Switches are exhaustive expressions with no {@code default} branch on purpose: add a
 * category and the compiler stops the build here instead of letting an unmapped failure reach a
 * client as a surprise 500.
 */
public final class ErrorStatusMapper {

    private ErrorStatusMapper() {
    }

    public static HttpStatus statusOf(ErrorCategory category) {
        return switch (category) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case AUTHENTICATION -> HttpStatus.UNAUTHORIZED;
            case AUTHORIZATION -> HttpStatus.FORBIDDEN;
            case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case INTEGRATION -> HttpStatus.BAD_GATEWAY;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * The {@code title} of the problem body.
     */
    public static String titleOf(ErrorCategory category) {
        return switch (category) {
            case VALIDATION -> "Validation error";
            case NOT_FOUND -> "Not found";
            case CONFLICT -> "Conflict";
            case AUTHENTICATION -> "Authentication failed";
            case AUTHORIZATION -> "Access denied";
            case BUSINESS_RULE -> "Business rule violated";
            case INTEGRATION -> "Upstream service error";
            case INTERNAL -> "Internal server error";
        };
    }

    /**
     * Goes into details field in ProblemDetail
     */
    public static String summaryOf(ErrorCategory category) {
        return switch (category) {
            case VALIDATION -> "Validation failed";
            case NOT_FOUND -> "Resource not found";
            case CONFLICT -> "Request conflicts with existing data";
            case AUTHENTICATION -> "Authentication failed";
            case AUTHORIZATION -> "Access denied";
            case BUSINESS_RULE -> "Request rejected by a business rule";
            case INTEGRATION -> "Upstream service error";
            case INTERNAL -> "Internal server error";
        };
    }
}
