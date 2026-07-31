package com.hutnyk.carfix.exception;

/**
 * A well-formed, authorized request the domain still refuses because a business rule says no, e.g.
 * booking outside opening hours, a service the branch does not offer, cancelling too late.
 * Maps to 422.
 */
public abstract class BusinessRuleViolationException extends CarFixException {

    protected BusinessRuleViolationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
