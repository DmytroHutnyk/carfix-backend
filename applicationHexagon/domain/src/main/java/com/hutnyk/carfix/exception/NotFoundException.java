package com.hutnyk.carfix.exception;

import lombok.Getter;

/**
 * A referenced resource does not exist or exists but is not visible to this caller. Maps to 404.
 * <p>
 * We use it for the not-visible case too: answering 403 there tells an attacker the id is real.
 * <p>
 * Abstract on purpose: each resource gets its own subtype and {@link ErrorCode}, so a client can
 * tell "no such car profile" from "no such booking" without parsing.
 */
@Getter
public abstract class NotFoundException extends CarFixException {

    //Nullable
    private final String resourceType;
    //Nullable
    private final Object resourceId;

    /**
     * Builds the conventional {@code "<resourceType> not found: <resourceId>"} message.
     *
     * @param resourceType human-readable resource name, e.g. {@code "Car profile"}
     * @param resourceId the identifier that missed; may be null when the lookup was not by id
     */
    protected NotFoundException(ErrorCode errorCode, String resourceType, Object resourceId) {
        super(errorCode, buildMessage(resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /** For lookups whose message does not fit the {@code type + id} shape. */
    protected NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.resourceType = null;
        this.resourceId = null;
    }

    private static String buildMessage(String resourceType, Object resourceId) {
        return resourceId == null
                ? resourceType + " not found"
                : resourceType + " not found: " + resourceId;
    }
}
