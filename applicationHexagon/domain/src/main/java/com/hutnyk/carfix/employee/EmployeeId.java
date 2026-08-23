package com.hutnyk.carfix.employee;

import com.hutnyk.carfix.util.Validator;
import java.util.UUID;

public record EmployeeId(UUID id) {
    public EmployeeId {
        Validator.notNull(id, "id");
    }

    public static EmployeeId of(UUID id) {
        return new EmployeeId(id);
    }

    public static EmployeeId genId() {
        return new EmployeeId(UUID.randomUUID());
    }
}
