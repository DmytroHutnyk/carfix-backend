package com.hutnyk.carfix.exception;

/** Well-formed, authorized request rejected by a business rule. Maps to 422. */
public abstract class BusinessRuleViolationException extends CarFixException {

    protected BusinessRuleViolationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
