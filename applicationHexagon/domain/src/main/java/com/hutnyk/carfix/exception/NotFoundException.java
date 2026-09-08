package com.hutnyk.carfix.exception;

import lombok.Getter;

/** Missing and hidden resources both map to 404, avoiding existence leaks. */
@Getter
public abstract class NotFoundException extends CarFixException {

    //Nullable
    private final String resourceType;
    //Nullable
    private final Object resourceId;

    protected NotFoundException(ErrorCode errorCode, String resourceType, Object resourceId) {
        super(errorCode, buildMessage(resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

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
