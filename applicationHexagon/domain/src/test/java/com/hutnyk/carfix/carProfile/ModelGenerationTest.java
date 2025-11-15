package com.hutnyk.carfix.carProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

public class ModelGenerationTest {

    @ParameterizedTest
    @ValueSource(ints = {10, 100})
    public void test_validateStartProductionDate_valid_date_and_fieldName(int yearsAgo) {
        //given
        Integer id = 1;
        String name = "Test Generation";
        LocalDate startProductionDate = LocalDate.now().minusYears(yearsAgo);
        LocalDate endProductionDate = null;
        Integer carModelId = 1;

        //when
        ModelGeneration result = ModelGeneration.of(id, name, startProductionDate, endProductionDate, carModelId);

        //then
        assertThat(result.getStartProductionDate()).isEqualTo(startProductionDate);
    }

    @Test
    public void test_validateStartProductionDate_throws_when_date_is_null() {
        //given
        Integer id = 1;
        String name = "Test Generation";
        LocalDate startProductionDate = null;
        LocalDate endProductionDate = null;
        Integer carModelId = 1;

        //when + then
        assertThatThrownBy(() -> ModelGeneration.of(id, name, startProductionDate, endProductionDate, carModelId))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }

    @Test
    public void test_validateStartProductionDate_throws_when_date_too_old() {
        //given
        Integer id = 1;
        String name = "Test Generation";
        LocalDate startProductionDate = LocalDate.now().minusYears(101);
        LocalDate endProductionDate = null;
        Integer carModelId = 1;

        //when + then
        assertThatThrownBy(() -> ModelGeneration.of(id, name, startProductionDate, endProductionDate, carModelId))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.DATE_TOO_OLD);
    }

    @Test
    public void test_validateEndProductionDate_valid_date_exactly_2_years_in_future() {
        //given
        Integer id = 1;
        String name = "Test Generation";
        LocalDate startProductionDate = LocalDate.now().minusYears(10);
        LocalDate endProductionDate = LocalDate.now().plusYears(2);
        Integer carModelId = 1;

        //when
        ModelGeneration result = ModelGeneration.of(id, name, startProductionDate, endProductionDate, carModelId);

        //then
        assertThat(result.getEndProductionDate()).isEqualTo(endProductionDate);
    }

    @Test
    public void test_validateEndProductionDate_returns_null_when_date_is_null() {
        //given
        Integer id = 1;
        String name = "Test Generation";
        LocalDate startProductionDate = LocalDate.now().minusYears(10);
        LocalDate endProductionDate = null;
        Integer carModelId = 1;

        //when
        ModelGeneration result = ModelGeneration.of(id, name, startProductionDate, endProductionDate, carModelId);

        //then
        assertThat(result.getEndProductionDate()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 10})
    public void test_validateEndProductionDate_throws_when_date_too_far_in_future(int yearsInFuture) {
        //given
        Integer id = 1;
        String name = "Test Generation";
        LocalDate startProductionDate = LocalDate.now().minusYears(10);
        LocalDate endProductionDate = LocalDate.now().plusYears(yearsInFuture);
        Integer carModelId = 1;

        //when + then
        assertThatThrownBy(() -> ModelGeneration.of(id, name, startProductionDate, endProductionDate, carModelId))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.DATE_TOO_FAR_IN_FUTURE);
    }
}

