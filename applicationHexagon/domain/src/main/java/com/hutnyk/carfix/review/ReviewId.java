package com.hutnyk.carfix.review;

import com.hutnyk.carfix.util.Validator;
import java.util.UUID;

public record ReviewId(UUID id) {
    public ReviewId {
        Validator.notNull(id, "id");
    }

    public static ReviewId of(UUID id) {
        return new ReviewId(id);
    }

    public static ReviewId genId() {
        return new ReviewId(UUID.randomUUID());
    }
}
