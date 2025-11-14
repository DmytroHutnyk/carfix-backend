package com.hutnyk.carfix.user;

import com.hutnyk.carfix.util.Validator;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@EqualsAndHashCode
@ToString(exclude = "value")
public final class PasswordHash {

    private final String value;

    private PasswordHash(String value) {
        this.value = validate(value);
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }

    private static String validate(String value) {
        Validator.notEmpty(value, "value");
        return value;
    }
}
