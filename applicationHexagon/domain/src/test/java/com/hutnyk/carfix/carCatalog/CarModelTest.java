package com.hutnyk.carfix.carCatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;

public class CarModelTest {

    @Test
    public void test_of_builds_model() {
        Integer id = 1;
        String name = "Camry";
        Integer carBrandId = 7;

        CarModel result = CarModel.of(id, name, carBrandId);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getCarBrandId()).isEqualTo(carBrandId);
    }

    @Test
    public void test_of_builds_model_with_null_id_when_not_persisted() {
        String name = "Camry";
        Integer carBrandId = 7;

        CarModel result = CarModel.of(null, name, carBrandId);

        assertThat(result.getId()).isNull();
    }

    @Test
    public void test_of_throws_when_carBrandId_is_null() {
        Integer id = 1;
        String name = "Camry";

        assertThatThrownBy(() -> CarModel.of(id, name, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }
}
