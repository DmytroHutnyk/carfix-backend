package com.hutnyk.carfix.scheduling.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.scheduling.TimeRange;
import io.hypersistence.utils.hibernate.type.range.Range;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class TimeRangeMapperTest {

    private static final LocalDateTime NINE = LocalDateTime.of(2026, 8, 12, 9, 0);
    private static final LocalDateTime SEVENTEEN = LocalDateTime.of(2026, 8, 12, 17, 0);

    @Test
    public void test_toDomain_maps_bounds() {
        Range<LocalDateTime> range = Range.closedOpen(NINE, SEVENTEEN);

        TimeRange result = TimeRangeMapper.toDomain(range);

        assertThat(result.lower()).isEqualTo(NINE);
        assertThat(result.upper()).isEqualTo(SEVENTEEN);
    }

    @Test
    public void test_toRange_produces_half_open_range() {
        TimeRange timeRange = TimeRange.of(NINE, SEVENTEEN);

        Range<LocalDateTime> result = TimeRangeMapper.toRange(timeRange);

        assertThat(result.lower()).isEqualTo(NINE);
        assertThat(result.upper()).isEqualTo(SEVENTEEN);
        assertThat(result.asString()).isEqualTo("[2026-08-12T09:00,2026-08-12T17:00)");
    }

    @Test
    public void test_round_trip_preserves_bounds() {
        TimeRange original = TimeRange.of(NINE, SEVENTEEN);

        TimeRange result = TimeRangeMapper.toDomain(TimeRangeMapper.toRange(original));

        assertThat(result).isEqualTo(original);
    }

    @Test
    public void test_toDomain_rejects_ranges_that_are_not_half_open() {
        assertThatThrownBy(() -> TimeRangeMapper.toDomain(Range.closed(NINE, SEVENTEEN)))
                .isInstanceOf(UnexpectedStateException.class);
        assertThatThrownBy(() -> TimeRangeMapper.toDomain(Range.open(NINE, SEVENTEEN)))
                .isInstanceOf(UnexpectedStateException.class);
        assertThatThrownBy(() -> TimeRangeMapper.toDomain(Range.openClosed(NINE, SEVENTEEN)))
                .isInstanceOf(UnexpectedStateException.class);
    }

    @Test
    public void test_null_inputs_map_to_null() {
        assertThat(TimeRangeMapper.toDomain(null)).isNull();
        assertThat(TimeRangeMapper.toRange(null)).isNull();
    }
}
