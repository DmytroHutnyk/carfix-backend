package com.hutnyk.carfix.scheduling.exception;

import com.hutnyk.carfix.exception.ValidationException;

/**
 * The slot query is not answerable as asked.
 * <p>
 * No field is named: the rules span the {@code serviceIds} / {@code from} / {@code to} triple,
 * so the reason belongs in the message rather than in a single-field {@code errors} map.
 */
public class InvalidSlotQueryException extends ValidationException {

    public InvalidSlotQueryException(String reason) {
        super(SchedulingErrorCode.INVALID_SLOT_QUERY, "Invalid slot query: " + reason, null, null);
    }
}
