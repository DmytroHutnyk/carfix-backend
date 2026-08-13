package com.hutnyk.carfix.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class TimeRangeTest {

    private static final LocalDateTime NINE = LocalDateTime.of(2026, 8, 12, 9, 0);
    private static final LocalDateTime SEVENTEEN = LocalDateTime.of(2026, 8, 12, 17, 0);

    @Test
    public void test_of_builds_range() {
        //when
        TimeRange result = TimeRange.of(NINE, SEVENTEEN);

        //then
        assertThat(result.lower()).isEqualTo(NINE);
        assertThat(result.upper()).isEqualTo(SEVENTEEN);
    }

    @Test
    public void test_of_throws_when_upper_equals_lower() {
        //when + then
        assertThatThrownBy(() -> TimeRange.of(NINE, NINE))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_TIME_RANGE);
    }

    @Test
    public void test_of_throws_when_upper_before_lower() {
        //when + then
        assertThatThrownBy(() -> TimeRange.of(SEVENTEEN, NINE))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_TIME_RANGE);
    }

    @Test
    public void test_of_throws_when_lower_is_null() {
        //when + then
        assertThatThrownBy(() -> TimeRange.of(null, SEVENTEEN))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }
}
