package com.hutnyk.carfix.openingHours;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.BranchId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class OpeningScheduleTest {

    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final LocalDate MONDAY = LocalDate.of(2026, 8, 17);

    private static OpeningHours hours(DayOfWeek day, int opens, int closes) {
        return OpeningHours.of(null, day, LocalTime.of(opens, 0), LocalTime.of(closes, 0), BRANCH_ID);
    }

    private static OpeningSchedule mondayEightToSix(List<OpeningHoursException> exceptions) {
        return OpeningSchedule.of(List.of(hours(DayOfWeek.MONDAY, 8, 18)), exceptions);
    }

    @Test
    void open_within_regular_hours_on_that_weekday() {
        //given
        OpeningSchedule schedule = mondayEightToSix(List.of());

        //when
        boolean open = schedule.isOpenAt(MONDAY.atTime(10, 30));

        //then
        assertThat(open).isTrue();
    }

    @Test
    void closed_before_opening_and_from_closing_time_on() {
        //given
        OpeningSchedule schedule = mondayEightToSix(List.of());

        //when + then
        assertThat(schedule.isOpenAt(MONDAY.atTime(7, 59))).isFalse();
        assertThat(schedule.isOpenAt(MONDAY.atTime(8, 0))).isTrue();
        assertThat(schedule.isOpenAt(MONDAY.atTime(18, 0))).isFalse();
    }

    @Test
    void closed_on_a_weekday_without_regular_hours() {
        //given
        OpeningSchedule schedule = mondayEightToSix(List.of());

        //when + then (2026-08-23 is a Sunday)
        assertThat(schedule.isOpenAt(MONDAY.plusDays(6).atTime(10, 0))).isFalse();
    }

    @Test
    void closing_exception_overrides_regular_hours_for_its_date() {
        //given
        OpeningHoursException holiday = OpeningHoursException.of(
                null, MONDAY, null, null, false, "Holiday", BRANCH_ID);
        OpeningSchedule schedule = mondayEightToSix(List.of(holiday));

        //when + then
        assertThat(schedule.isOpenAt(MONDAY.atTime(10, 0))).isFalse();
        assertThat(schedule.isOpenAt(MONDAY.plusDays(7).atTime(10, 0))).isTrue();
    }

    @Test
    void opening_exception_replaces_regular_hours_for_its_date() {
        //given
        OpeningHoursException special = OpeningHoursException.of(
                null, MONDAY, LocalTime.of(12, 0), LocalTime.of(14, 0), true, null, BRANCH_ID);
        OpeningSchedule schedule = mondayEightToSix(List.of(special));

        //when + then
        assertThat(schedule.isOpenAt(MONDAY.atTime(10, 0))).isFalse();
        assertThat(schedule.isOpenAt(MONDAY.atTime(13, 0))).isTrue();
    }

    @Test
    void empty_schedule_is_always_closed() {
        //given
        OpeningSchedule schedule = OpeningSchedule.of(List.of(), List.of());

        //when + then
        assertThat(schedule.isOpenAt(MONDAY.atTime(10, 0))).isFalse();
    }
}
