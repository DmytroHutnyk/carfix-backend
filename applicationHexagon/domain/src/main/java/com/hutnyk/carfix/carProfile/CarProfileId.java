package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.util.Validator;
import java.util.UUID;

public record CarProfileId(UUID id) {
    public CarProfileId {
        Validator.notNull(id, "id");
    }

    public static CarProfileId of(UUID id) {
        return new CarProfileId(id);
    }

    public static CarProfileId genId() {
        return new CarProfileId(UUID.randomUUID());
    }
}
