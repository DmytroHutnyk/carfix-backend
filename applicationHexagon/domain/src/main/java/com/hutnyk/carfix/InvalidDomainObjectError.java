package com.hutnyk.carfix;

public class InvalidDomainObjectError extends RuntimeException {
    public InvalidDomainObjectError(String message) {
        super(message);
    }
}
