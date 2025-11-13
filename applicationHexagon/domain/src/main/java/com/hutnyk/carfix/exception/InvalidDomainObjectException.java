package com.hutnyk.carfix.exception;

/**
 *  {@code InvalidDomainObjectException} is the superclass of exceptions in domain module.
 */
public class InvalidDomainObjectException extends RuntimeException {
    public InvalidDomainObjectException(String message) {
        super(message);
    }
}
