package com.hutnyk.carfix.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TimeRangesTest {

    private static TimeRange range(int fromHour, int fromMin, int toHour, int toMin) {
        return TimeRange.of(
                LocalDateTime.of(2026, 8, 13, fromHour, fromMin),
                LocalDateTime.of(2026, 8, 13, toHour, toMin));
    }

    private static TimeRange range(int fromHour, int toHour) {
        return range(fromHour, 0, toHour, 0);
    }

    @Test
    void test_union_empty_input_gives_empty() {
        assertThat(TimeRanges.union(List.of())).isEmpty();
    }

    @Test
    void test_union_sorts_disjoint_ranges() {
        assertThat(TimeRanges.union(List.of(range(14, 16), range(9, 12))))
                .containsExactly(range(9, 12), range(14, 16));
    }

    @Test
    void test_union_merges_overlapping_and_adjacent() {
        assertThat(TimeRanges.union(List.of(range(9, 12), range(11, 14), range(14, 16))))
                .containsExactly(range(9, 16));
    }

    @Test
    void test_union_keeps_contained_range_absorbed() {
        assertThat(TimeRanges.union(List.of(range(9, 17), range(10, 11))))
                .containsExactly(range(9, 17));
    }

    @Test
    void test_subtractAll_no_cuts_returns_unioned_base() {
        assertThat(TimeRanges.subtractAll(List.of(range(9, 12), range(12, 14)), List.of()))
                .containsExactly(range(9, 14));
    }

    @Test
    void test_subtractAll_multiple_cuts_split_base() {
        assertThat(TimeRanges.subtractAll(
                List.of(range(9, 17)),
                List.of(range(10, 11), range(12, 30, 13, 0))))
                .containsExactly(range(9, 10), range(11, 0, 12, 30), range(13, 17));
    }

    @Test
    void test_subtractAll_cut_covering_everything_gives_empty() {
        assertThat(TimeRanges.subtractAll(List.of(range(10, 12)), List.of(range(8, 18))))
                .isEmpty();
    }

    @Test
    void test_free_is_availability_minus_occupancy() {
        List<TimeRange> availability = List.of(range(9, 12), range(13, 17));
        List<TimeRange> occupancy = List.of(range(10, 0, 10, 30), range(16, 17));
        assertThat(TimeRanges.free(availability, occupancy))
                .containsExactly(range(9, 10), range(10, 30, 12, 0), range(13, 16));
    }

    @Test
    void test_intersect_disjoint_lists_is_empty() {
        //given
        List<TimeRange> a = List.of(range(9, 12));
        List<TimeRange> b = List.of(range(12, 14));
        //when
        List<TimeRange> result = TimeRanges.intersect(a, b);
        //then
        assertThat(result).isEmpty();
    }

    @Test
    void test_intersect_keeps_only_the_overlap() {
        //given
        List<TimeRange> a = List.of(range(9, 12));
        List<TimeRange> b = List.of(range(10, 14));
        //when
        List<TimeRange> result = TimeRanges.intersect(a, b);
        //then
        assertThat(result).containsExactly(range(10, 12));
    }

    @Test
    void test_intersect_splits_across_pieces_of_the_other_list() {
        //given
        List<TimeRange> a = List.of(range(8, 18));
        List<TimeRange> b = List.of(range(9, 12), range(13, 17));
        //when
        List<TimeRange> result = TimeRanges.intersect(a, b);
        //then
        assertThat(result).containsExactly(range(9, 12), range(13, 17));
    }

    @Test
    void test_intersect_unions_unsorted_overlapping_inputs_first() {
        //given
        List<TimeRange> a = List.of(range(11, 14), range(9, 12));
        List<TimeRange> b = List.of(range(10, 30, 13, 0), range(8, 10));
        //when
        List<TimeRange> result = TimeRanges.intersect(a, b);
        //then
        assertThat(result).containsExactly(range(9, 0, 10, 0), range(10, 30, 13, 0));
    }

    @Test
    void test_intersect_with_empty_list_is_empty() {
        assertThat(TimeRanges.intersect(List.of(range(9, 12)), List.of())).isEmpty();
        assertThat(TimeRanges.intersect(List.of(), List.of(range(9, 12)))).isEmpty();
    }
}
