package com.hutnyk.carfix.branch;

import java.time.Duration;

public enum CancellationPolicy {

    STRICT(Duration.ofHours(48)),
    MODERATE(Duration.ofHours(24)),
    FLEXIBLE(Duration.ofHours(2));

    private final Duration notice;

    CancellationPolicy(Duration notice) {
        this.notice = notice;
    }

    public Duration notice() {
        return notice;
    }
}
