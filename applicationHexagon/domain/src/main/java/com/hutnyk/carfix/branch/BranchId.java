package com.hutnyk.carfix.branch;

import com.hutnyk.carfix.util.Validator;
import java.util.UUID;

public record BranchId(UUID id) {
    public BranchId {
        Validator.notNull(id, "id");
    }

    public static BranchId of(UUID id) {
        return new BranchId(id);
    }

    public static BranchId genId() {
        return new BranchId(UUID.randomUUID());
    }
}
