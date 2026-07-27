package com.hutnyk.carfix.carCatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;

public class CarBrandTest {

    @Test
    public void test_of_builds_brand() {
        //given
        Integer id = 1;
        String name = "Toyota";

        //when
        CarBrand result = CarBrand.of(id, name);

        //then
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    public void test_of_throws_when_name_is_blank() {
        //given
        Integer id = 1;
        String name = "   ";

        //when + then
        assertThatThrownBy(() -> CarBrand.of(id, name))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.EMPTY_STRING);
    }

    @Test
    public void test_of_throws_when_name_is_null() {
        //given
        Integer id = 1;
        String name = null;

        //when + then
        assertThatThrownBy(() -> CarBrand.of(id, name))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }
}
