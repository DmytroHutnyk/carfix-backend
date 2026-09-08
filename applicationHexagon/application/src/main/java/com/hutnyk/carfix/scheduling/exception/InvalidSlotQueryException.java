package com.hutnyk.carfix.scheduling.exception;

import com.hutnyk.carfix.exception.ValidationException;

/** Cross-field slot-query failures stay in message instead of blaming one field. */
public class InvalidSlotQueryException extends ValidationException {

    public InvalidSlotQueryException(String reason) {
        super(SchedulingErrorCode.INVALID_SLOT_QUERY, "Invalid slot query: " + reason, null, null);
    }
}
