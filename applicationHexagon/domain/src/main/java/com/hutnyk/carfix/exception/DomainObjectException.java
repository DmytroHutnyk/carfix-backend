package com.hutnyk.carfix.exception;

/**
 *  {@code InvalidDomainObjectException} is the superclass of exceptions in domain module.
 */
public class DomainObjectException extends RuntimeException {
    public DomainObjectException(String message) {
        super(message);
    }
}
