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

    private static TimeRange range(int fromHour, int toHour) {
        return TimeRange.of(
                LocalDateTime.of(2026, 8, 13, fromHour, 0),
                LocalDateTime.of(2026, 8, 13, toHour, 0));
    }

    @Test
    void test_overlaps_true_on_partial_overlap_false_on_touching() {
        assertThat(range(9, 12).overlaps(range(11, 14))).isTrue();
        assertThat(range(9, 12).overlaps(range(12, 14))).isFalse();
        assertThat(range(9, 12).overlaps(range(13, 14))).isFalse();
    }

    @Test
    void test_contains_inside_and_equal_true_overhang_false() {
        assertThat(range(9, 17).contains(range(10, 12))).isTrue();
        assertThat(range(9, 17).contains(range(9, 17))).isTrue();
        assertThat(range(9, 17).contains(range(8, 12))).isFalse();
        assertThat(range(9, 17).contains(range(16, 18))).isFalse();
    }

    @Test
    void test_intersect_disjoint_and_touching_empty() {
        assertThat(range(9, 10).intersect(range(11, 12))).isEmpty();
        assertThat(range(9, 10).intersect(range(10, 12))).isEmpty();
    }

    @Test
    void test_intersect_overlap_trims_to_common_part() {
        assertThat(range(9, 12).intersect(range(10, 14))).contains(range(10, 12));
        assertThat(range(9, 17).intersect(range(10, 12))).contains(range(10, 12));
        assertThat(range(9, 12).intersect(range(9, 12))).contains(range(9, 12));
    }

    @Test
    void test_subtract_disjoint_returns_self() {
        assertThat(range(9, 12).subtract(range(13, 14))).containsExactly(range(9, 12));
        assertThat(range(9, 12).subtract(range(12, 14))).containsExactly(range(9, 12));
    }

    @Test
    void test_subtract_covering_returns_empty() {
        assertThat(range(10, 12).subtract(range(9, 13))).isEmpty();
        assertThat(range(10, 12).subtract(range(10, 12))).isEmpty();
    }

    @Test
    void test_subtract_middle_splits_in_two() {
        assertThat(range(9, 17).subtract(range(12, 13)))
                .containsExactly(range(9, 12), range(13, 17));
    }

    @Test
    void test_subtract_edge_overlaps_trim_one_side() {
        assertThat(range(9, 17).subtract(range(8, 12))).containsExactly(range(12, 17));
        assertThat(range(9, 17).subtract(range(15, 18))).containsExactly(range(9, 15));
    }
}
