package com.hutnyk.carfix.error;

import com.hutnyk.carfix.exception.ErrorCategory;
import org.springframework.http.HttpStatus;

/** No default branches: new categories must fail compilation until mapped. */
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
