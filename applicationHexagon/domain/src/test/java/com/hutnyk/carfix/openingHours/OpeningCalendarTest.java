package com.hutnyk.carfix.openingHours;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.scheduling.TimeRange;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class OpeningCalendarTest {

    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final LocalDate FRIDAY = LocalDate.of(2026, 8, 14);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 8, 15);
    private static final LocalDate SUNDAY = LocalDate.of(2026, 8, 16);

    private static OpeningHours weekly(DayOfWeek day, int open, int close) {
        return OpeningHours.of(null, day, LocalTime.of(open, 0), LocalTime.of(close, 0), BRANCH_ID);
    }

    private static List<OpeningHours> monToFri(int open, int close) {
        return List.of(
                weekly(DayOfWeek.MONDAY, open, close), weekly(DayOfWeek.TUESDAY, open, close),
                weekly(DayOfWeek.WEDNESDAY, open, close), weekly(DayOfWeek.THURSDAY, open, close),
                weekly(DayOfWeek.FRIDAY, open, close));
    }

    private static OpeningHoursException closedOn(LocalDate date) {
        return OpeningHoursException.of(null, date, null, null, false, "holiday", BRANCH_ID);
    }

    private static OpeningHoursException openOn(LocalDate date, int open, int close) {
        return OpeningHoursException.of(null, date, LocalTime.of(open, 0), LocalTime.of(close, 0), true, null, BRANCH_ID);
    }

    private static TimeRange range(LocalDate date, int open, int close) {
        return TimeRange.of(date.atTime(open, 0), date.atTime(close, 0));
    }

    @Test
    void test_weekday_with_weekly_row_is_open_for_that_row() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of());
        //when
        List<TimeRange> open = calendar.openRanges(FRIDAY);
        //then
        assertThat(open).containsExactly(range(FRIDAY, 8, 18));
        assertThat(calendar.isOpen(FRIDAY)).isTrue();
    }

    @Test
    void test_weekday_without_weekly_row_is_closed() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of());
        //when //then
        assertThat(calendar.openRanges(SATURDAY)).isEmpty();
        assertThat(calendar.isOpen(SUNDAY)).isFalse();
    }

    @Test
    void test_no_weekly_rows_means_closed_every_day() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(List.of(), List.of());
        //when //then
        assertThat(calendar.openRanges(FRIDAY)).isEmpty();
    }

    @Test
    void test_several_weekly_rows_for_one_day_are_merged_and_sorted() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(List.of(
                weekly(DayOfWeek.FRIDAY, 13, 17),
                weekly(DayOfWeek.FRIDAY, 8, 12),
                weekly(DayOfWeek.FRIDAY, 11, 12)), List.of());
        //when
        List<TimeRange> open = calendar.openRanges(FRIDAY);
        //then
        assertThat(open).containsExactly(range(FRIDAY, 8, 12), range(FRIDAY, 13, 17));
    }

    @Test
    void test_closed_exception_closes_a_normally_open_day() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(closedOn(FRIDAY)));
        //when //then
        assertThat(calendar.openRanges(FRIDAY)).isEmpty();
        assertThat(calendar.isOpen(FRIDAY)).isFalse();
    }

    @Test
    void test_open_exception_opens_a_normally_closed_day() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(openOn(SATURDAY, 9, 13)));
        //when //then
        assertThat(calendar.openRanges(SATURDAY)).containsExactly(range(SATURDAY, 9, 13));
    }

    @Test
    void test_open_exception_replaces_the_weekly_hours_of_its_date() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(openOn(FRIDAY, 10, 12)));
        //when //then
        assertThat(calendar.openRanges(FRIDAY)).containsExactly(range(FRIDAY, 10, 12));
    }

    @Test
    void test_two_open_exceptions_on_one_date_are_merged() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18),
                List.of(openOn(FRIDAY, 14, 16), openOn(FRIDAY, 9, 11)));
        //when //then
        assertThat(calendar.openRanges(FRIDAY)).containsExactly(range(FRIDAY, 9, 11), range(FRIDAY, 14, 16));
    }

    @Test
    void test_closed_exception_wins_over_open_exception_on_the_same_date() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18),
                List.of(openOn(FRIDAY, 9, 11), closedOn(FRIDAY)));
        //when //then
        assertThat(calendar.openRanges(FRIDAY)).isEmpty();
    }

    @Test
    void test_exception_only_affects_its_own_date() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(closedOn(FRIDAY.minusDays(1))));
        //when //then
        assertThat(calendar.openRanges(FRIDAY)).containsExactly(range(FRIDAY, 8, 18));
    }

    @Test
    void test_row_with_close_not_after_start_fails_loud_when_applied() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(List.of(weekly(DayOfWeek.FRIDAY, 18, 8)), List.of());
        //when //then
        assertThatThrownBy(() -> calendar.openRanges(FRIDAY))
                .isInstanceOf(DomainObjectValidationException.class);
        assertThat(calendar.openRanges(SATURDAY)).isEmpty();
    }

    @Test
    void test_null_inputs_rejected() {
        assertThatThrownBy(() -> OpeningCalendar.of(null, List.of()))
                .isInstanceOf(DomainObjectValidationException.class);
        assertThatThrownBy(() -> OpeningCalendar.of(List.of(), null))
                .isInstanceOf(DomainObjectValidationException.class);
    }
}
