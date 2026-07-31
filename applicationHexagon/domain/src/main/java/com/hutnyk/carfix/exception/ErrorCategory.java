package com.hutnyk.carfix.exception;

/**
 * The domain names the <em>kind</em> of failure; the web adapter alone decides which HTTP status
 * each kind becomes. Adding a constant here is deliberate: the adapter maps categories with an
 * exhaustive {@code switch}, so an unmapped category fails the build rather than the request.
 */
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
