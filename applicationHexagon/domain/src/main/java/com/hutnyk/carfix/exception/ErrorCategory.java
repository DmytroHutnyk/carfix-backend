package com.hutnyk.carfix.exception;

/** Web adapter maps these with an exhaustive switch, making unmapped categories fail compilation. */
public enum ErrorCategory {

    /** Input did not satisfy a format or invariant rule. */
    VALIDATION,

    /** A referenced resource does not exist, or is not visible to the caller. */
    NOT_FOUND,

    /** The request collides with state that already exists (uniqueness, double booking). */
    CONFLICT,

    /** The caller's identity could not be established, or no longer holds. */
    AUTHENTICATION,

    /** The caller is known but not permitted to do this. */
    AUTHORIZATION,

    /** A well-formed, permitted request the domain still refuses on a business rule. */
    BUSINESS_RULE,

    /** An outbound dependency (payment, mail, geocoding) failed or misbehaved. */
    INTEGRATION,

    /** An invariant of our own code broke. Never the caller's fault. */
    INTERNAL
}
