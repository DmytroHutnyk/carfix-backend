package com.hutnyk.carfix.carCatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;

public class ModelVersionTest {

    @ParameterizedTest
    @ValueSource(ints = {10, 100})
    public void test_validateStartProduction_valid_year_and_fieldName(int yearsAgo) {
        Integer id = 1;
        String name = "Test Version";
        Short startProduction = (short) (Year.now().getValue() - yearsAgo);
        Short endProduction = null;
        Integer carModelId = 1;

        ModelVersion result = ModelVersion.of(id, name, startProduction, endProduction, carModelId);

        assertThat(result.getStartProduction()).isEqualTo(startProduction);
    }

    @Test
    public void test_validateStartProduction_throws_when_year_is_null() {
        Integer id = 1;
        String name = "Test Version";
        Short startProduction = null;
        Short endProduction = null;
        Integer carModelId = 1;

        assertThatThrownBy(() -> ModelVersion.of(id, name, startProduction, endProduction, carModelId))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }

    @Test
    public void test_validateStartProduction_throws_when_year_too_old() {
        Integer id = 1;
        String name = "Test Version";
        Short startProduction = (short) (Year.now().getValue() - 101);
        Short endProduction = null;
        Integer carModelId = 1;

        assertThatThrownBy(() -> ModelVersion.of(id, name, startProduction, endProduction, carModelId))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.DATE_TOO_OLD);
    }

    @Test
    public void test_validateEndProduction_valid_year_exactly_2_years_in_future() {
        Integer id = 1;
        String name = "Test Version";
        Short startProduction = (short) (Year.now().getValue() - 10);
        Short endProduction = (short) (Year.now().getValue() + 2);
        Integer carModelId = 1;

        ModelVersion result = ModelVersion.of(id, name, startProduction, endProduction, carModelId);

        assertThat(result.getEndProduction()).isEqualTo(endProduction);
    }

    @Test
    public void test_validateEndProduction_returns_null_when_year_is_null() {
        Integer id = 1;
        String name = "Test Version";
        Short startProduction = (short) (Year.now().getValue() - 10);
        Short endProduction = null;
        Integer carModelId = 1;

        ModelVersion result = ModelVersion.of(id, name, startProduction, endProduction, carModelId);

        assertThat(result.getEndProduction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 10})
    public void test_validateEndProduction_throws_when_year_too_far_in_future(int yearsInFuture) {
        Integer id = 1;
        String name = "Test Version";
        Short startProduction = (short) (Year.now().getValue() - 10);
        Short endProduction = (short) (Year.now().getValue() + yearsInFuture);
        Integer carModelId = 1;

        assertThatThrownBy(() -> ModelVersion.of(id, name, startProduction, endProduction, carModelId))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.DATE_TOO_FAR_IN_FUTURE);
    }
}

