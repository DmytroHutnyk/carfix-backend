package com.hutnyk.carfix.openingHours;

import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.scheduling.TimeRanges;
import com.hutnyk.carfix.util.Validator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OpeningCalendar {

    private final Map<DayOfWeek, List<OpeningHours>> weekly;
    private final Map<LocalDate, List<OpeningHoursException>> exceptions;

    private OpeningCalendar(Map<DayOfWeek, List<OpeningHours>> weekly,
                            Map<LocalDate, List<OpeningHoursException>> exceptions) {
        this.weekly = weekly;
        this.exceptions = exceptions;
    }

    public static OpeningCalendar of(List<OpeningHours> weekly, List<OpeningHoursException> exceptions) {
        Validator.notNull(weekly, "weekly");
        Validator.notNull(exceptions, "exceptions");
        Map<DayOfWeek, List<OpeningHours>> byDay = new EnumMap<>(DayOfWeek.class);
        weekly.forEach(row -> byDay.computeIfAbsent(row.getDayOfWeek(), d -> new ArrayList<>()).add(row));
        Map<LocalDate, List<OpeningHoursException>> byDate = new HashMap<>();
        exceptions.forEach(row -> byDate.computeIfAbsent(row.getDate(), d -> new ArrayList<>()).add(row));
        return new OpeningCalendar(byDay, byDate);
    }

    // Returns merged branch-local ranges in ascending order; empty means closed all day.
    public List<TimeRange> openRanges(LocalDate date) {
        List<OpeningHoursException> forDate = exceptions.getOrDefault(date, List.of());
        if (!forDate.isEmpty()) {
            if (forDate.stream().anyMatch(row -> !row.getIsOpen())) {
                return List.of();
            }
            return TimeRanges.union(forDate.stream()
                    .map(row -> range(date, row.getStartTime(), row.getCloseTime()))
                    .toList());
        }
        return TimeRanges.union(weekly.getOrDefault(dayOfWeek(date), List.of()).stream()
                .map(row -> range(date, row.getStartTime(), row.getCloseTime()))
                .toList());
    }

    public boolean isOpen(LocalDate date) {
        return !openRanges(date).isEmpty();
    }

    public boolean isOpenAt(LocalDateTime moment) {
        Validator.notNull(moment, "moment");
        return openRanges(moment.toLocalDate()).stream().anyMatch(range -> range.contains(moment));
    }

    public Map<LocalDate, List<TimeRange>> openRangesByDate(LocalDate from, LocalDate to) {
        Map<LocalDate, List<TimeRange>> openByDate = new LinkedHashMap<>();
        for (LocalDate date : from.datesUntil(to.plusDays(1)).toList()) {
            List<TimeRange> open = openRanges(date);
            if (!open.isEmpty()) {
                openByDate.put(date, open);
            }
        }
        return Collections.unmodifiableMap(openByDate);
    }

    private static TimeRange range(LocalDate date, LocalTime start, LocalTime close) {
        return TimeRange.of(date.atTime(start), date.atTime(close));
    }

    private static DayOfWeek dayOfWeek(LocalDate date) {
        return DayOfWeek.valueOf(date.getDayOfWeek().name());
    }
}
