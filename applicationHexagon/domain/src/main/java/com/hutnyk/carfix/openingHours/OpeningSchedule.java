package com.hutnyk.carfix.openingHours;

import com.hutnyk.carfix.util.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class OpeningSchedule {

    private final Map<DayOfWeek, List<OpeningHours>> regularHours;
    private final Map<LocalDate, OpeningHoursException> exceptions;

    private OpeningSchedule(List<OpeningHours> regularHours, List<OpeningHoursException> exceptions) {
        this.regularHours = regularHours.stream()
                .collect(Collectors.groupingBy(OpeningHours::getDayOfWeek));
        this.exceptions = exceptions.stream()
                .collect(Collectors.toMap(
                        OpeningHoursException::getDate, Function.identity(), (first, second) -> first));
    }

    public static OpeningSchedule of(List<OpeningHours> regularHours, List<OpeningHoursException> exceptions) {
        return new OpeningSchedule(
                Validator.notNull(regularHours, "regularHours"),
                Validator.notNull(exceptions, "exceptions"));
    }

    public boolean isOpenAt(LocalDateTime localDateTime) {
        Validator.notNull(localDateTime, "localDateTime");
        LocalTime time = localDateTime.toLocalTime();
        OpeningHoursException exception = exceptions.get(localDateTime.toLocalDate());
        if (exception != null) {
            return exception.getIsOpen()
                    && within(exception.getStartTime(), exception.getCloseTime(), time);
        }
        DayOfWeek day = DayOfWeek.valueOf(localDateTime.getDayOfWeek().name());
        return regularHours.getOrDefault(day, List.of()).stream()
                .anyMatch(hours -> within(hours.getStartTime(), hours.getCloseTime(), time));
    }

    private static boolean within(LocalTime start, LocalTime close, LocalTime time) {
        return !time.isBefore(start) && time.isBefore(close);
    }
}
