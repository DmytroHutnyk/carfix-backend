package com.hutnyk.carfix.search.exception;

import com.hutnyk.carfix.exception.ValidationException;

/**
 * The search filter combination is not usable.
 * <p>
 * No field is named: the rule spans the {@code q} / {@code serviceName} / {@code categoryId} triple,
 * so the reason belongs in the message rather than in a single-field {@code errors} map.
 */
public class InvalidSearchFilterException extends ValidationException {

    public InvalidSearchFilterException(String reason) {
        super(SearchErrorCode.INVALID_SEARCH_FILTER, "Invalid search filter: " + reason, null, null);
    }
}
