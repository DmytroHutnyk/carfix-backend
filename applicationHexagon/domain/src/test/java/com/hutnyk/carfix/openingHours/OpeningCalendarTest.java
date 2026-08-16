package com.hutnyk.carfix.openingHours;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.scheduling.TimeRange;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class OpeningCalendarTest {

    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final LocalDate FRIDAY = LocalDate.of(2026, 8, 14);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 8, 15);
    private static final LocalDate SUNDAY = LocalDate.of(2026, 8, 16);
    private static final LocalDate MONDAY = LocalDate.of(2026, 8, 17);
    private static final LocalDate TUESDAY = LocalDate.of(2026, 8, 18);
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 8, 19);

    private static OpeningHours weekly(DayOfWeek day, int open, int close) {
        return OpeningHours.of(null, day, LocalTime.of(open, 0), LocalTime.of(close, 0), OpeningHoursMode.OPEN, BRANCH_ID);
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
        OpeningCalendar openThenClosed = OpeningCalendar.of(monToFri(8, 18),
                List.of(openOn(FRIDAY, 9, 11), closedOn(FRIDAY)));
        OpeningCalendar closedThenOpen = OpeningCalendar.of(monToFri(8, 18),
                List.of(closedOn(FRIDAY), openOn(FRIDAY, 9, 11)));
        //when //then
        assertThat(openThenClosed.openRanges(FRIDAY)).isEmpty();
        assertThat(closedThenOpen.openRanges(FRIDAY)).isEmpty();
    }

    @Test
    void test_exception_only_affects_its_own_date() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(closedOn(FRIDAY.minusDays(1))));
        //when //then
        assertThat(calendar.openRanges(FRIDAY)).containsExactly(range(FRIDAY, 8, 18));
    }

    @Test
    void test_exception_is_keyed_by_date_not_by_weekday() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(closedOn(FRIDAY)));
        //when //then
        assertThat(calendar.openRanges(FRIDAY)).isEmpty();
        assertThat(calendar.openRanges(FRIDAY.plusWeeks(1)))
                .containsExactly(range(FRIDAY.plusWeeks(1), 8, 18));
    }

    @Test
    void test_open_ranges_by_date_keeps_only_open_dates_in_order() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of());
        //when
        Map<LocalDate, List<TimeRange>> openByDate = calendar.openRangesByDate(SATURDAY, WEDNESDAY);
        //then
        assertThat(openByDate).containsExactly(
                entry(MONDAY, List.of(range(MONDAY, 8, 18))),
                entry(TUESDAY, List.of(range(TUESDAY, 8, 18))),
                entry(WEDNESDAY, List.of(range(WEDNESDAY, 8, 18))));
    }

    @Test
    void test_open_ranges_by_date_drops_a_date_closed_by_exception() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(closedOn(TUESDAY)));
        //when
        Map<LocalDate, List<TimeRange>> openByDate = calendar.openRangesByDate(SATURDAY, WEDNESDAY);
        //then
        assertThat(openByDate).containsOnlyKeys(MONDAY, WEDNESDAY);
    }

    @Test
    void test_open_ranges_by_date_adds_a_date_opened_by_exception() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(openOn(SATURDAY, 9, 13)));
        //when
        Map<LocalDate, List<TimeRange>> openByDate = calendar.openRangesByDate(SATURDAY, WEDNESDAY);
        //then
        assertThat(openByDate).containsOnlyKeys(SATURDAY, MONDAY, TUESDAY, WEDNESDAY);
        assertThat(openByDate.get(SATURDAY)).containsExactly(range(SATURDAY, 9, 13));
    }

    @Test
    void test_is_open_at_includes_the_start_and_excludes_the_close() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of());
        //when //then
        assertThat(calendar.isOpenAt(MONDAY.atTime(7, 59))).isFalse();
        assertThat(calendar.isOpenAt(MONDAY.atTime(8, 0))).isTrue();
        assertThat(calendar.isOpenAt(MONDAY.atTime(10, 30))).isTrue();
        assertThat(calendar.isOpenAt(MONDAY.atTime(18, 0))).isFalse();
    }

    @Test
    void test_is_open_at_is_false_on_a_weekday_without_rows() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of());
        //when //then
        assertThat(calendar.isOpenAt(SUNDAY.atTime(10, 0))).isFalse();
    }

    @Test
    void test_is_open_at_follows_a_closed_exception() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(closedOn(MONDAY)));
        //when //then
        assertThat(calendar.isOpenAt(MONDAY.atTime(10, 0))).isFalse();
        assertThat(calendar.isOpenAt(MONDAY.plusWeeks(1).atTime(10, 0))).isTrue();
    }

    @Test
    void test_is_open_at_follows_an_open_exception() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(monToFri(8, 18), List.of(openOn(MONDAY, 12, 14)));
        //when //then
        assertThat(calendar.isOpenAt(MONDAY.atTime(10, 0))).isFalse();
        assertThat(calendar.isOpenAt(MONDAY.atTime(13, 0))).isTrue();
    }

    @Test
    void test_is_open_at_is_false_without_any_rows() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(List.of(), List.of());
        //when //then
        assertThat(calendar.isOpenAt(MONDAY.atTime(10, 0))).isFalse();
    }

    @Test
    void test_row_with_close_not_after_start_fails_loud_when_applied() {
        //given
        OpeningCalendar calendar = OpeningCalendar.of(List.of(weekly(DayOfWeek.FRIDAY, 18, 8)), List.of());
        //when //then
        assertThatThrownBy(() -> calendar.openRanges(FRIDAY))
                .isInstanceOfSatisfying(DomainObjectValidationException.class,
                        e -> assertThat(e.getErrorType()).isEqualTo(ValidationErrorType.INVALID_TIME_RANGE));
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
