package com.hutnyk.carfix.exception;

/** Stable client-visible failure code, grouped by HTTP-mapped category. */
public interface ErrorCode {

    String code();

    ErrorCategory category();
}
