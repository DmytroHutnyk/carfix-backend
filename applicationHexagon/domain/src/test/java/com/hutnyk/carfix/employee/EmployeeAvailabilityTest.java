package com.hutnyk.carfix.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.user.UserId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class EmployeeAvailabilityTest {

    private static final LocalDate DATE = LocalDate.of(2026, 8, 12);
    private static final TimeRange RANGE = TimeRange.of(
            LocalDateTime.of(2026, 8, 12, 9, 0), LocalDateTime.of(2026, 8, 12, 17, 0));
    private static final UserId EMPLOYEE_ID = UserId.of(UUID.randomUUID());

    @Test
    public void test_of_builds_availability_with_series() {
        //when
        EmployeeAvailability result = EmployeeAvailability.of(1, RANGE, DATE, 42, EMPLOYEE_ID);

        //then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getAvailableTime()).isEqualTo(RANGE);
        assertThat(result.getDate()).isEqualTo(DATE);
        assertThat(result.getSeriesId()).isEqualTo(42);
        assertThat(result.getEmployeeId()).isEqualTo(EMPLOYEE_ID);
    }

    @Test
    public void test_of_allows_null_series_for_one_off_entry() {
        //when
        EmployeeAvailability result = EmployeeAvailability.of(1, RANGE, DATE, null, EMPLOYEE_ID);

        //then
        assertThat(result.getSeriesId()).isNull();
    }

    @Test
    public void test_of_throws_when_available_time_is_null() {
        //when + then
        assertThatThrownBy(() -> EmployeeAvailability.of(1, null, DATE, 42, EMPLOYEE_ID))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }
}
