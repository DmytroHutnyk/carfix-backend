package com.hutnyk.carfix.employee;

import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EmployeeAvailability {

    @EqualsAndHashCode.Include
    private final Integer id;

    private final TimeRange availableTime;
    private final LocalDate date;

    //Nullable — null for a one-off entry, set for every row of one recurrence series
    private final Integer seriesId;
    private final EmployeeId employeeId;

    @Builder
    private EmployeeAvailability(
            Integer id, TimeRange availableTime, LocalDate date, Integer seriesId, EmployeeId employeeId) {
        this.id = id;
        this.availableTime = Validator.notNull(availableTime, "availableTime");
        this.date = Validator.notNull(date, "date");
        this.seriesId = seriesId;
        this.employeeId = Validator.notNull(employeeId, "employeeId");
    }

    public static EmployeeAvailability of(
            Integer id, TimeRange availableTime, LocalDate date, Integer seriesId, EmployeeId employeeId) {
        return EmployeeAvailability.builder()
                .id(id)
                .availableTime(availableTime)
                .date(date)
                .seriesId(seriesId)
                .employeeId(employeeId)
                .build();
    }
}
