package com.hutnyk.carfix.carCatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;

public class CarBrandTest {

    @Test
    public void test_of_builds_brand() {
        Integer id = 1;
        String name = "Toyota";

        CarBrand result = CarBrand.of(id, name);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    public void test_of_throws_when_name_is_blank() {
        Integer id = 1;
        String name = "   ";

        assertThatThrownBy(() -> CarBrand.of(id, name))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @Test
    public void test_of_throws_when_name_is_null() {
        Integer id = 1;
        String name = null;

        assertThatThrownBy(() -> CarBrand.of(id, name))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }
}
