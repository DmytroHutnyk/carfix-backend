package com.hutnyk.carfix.search.exception;

import com.hutnyk.carfix.exception.ValidationException;

/** Cross-field search failures stay in message instead of blaming one field. */
public class InvalidSearchFilterException extends ValidationException {

    public InvalidSearchFilterException(String reason) {
        super(SearchErrorCode.INVALID_SEARCH_FILTER, "Invalid search filter: " + reason, null, null);
    }
}
