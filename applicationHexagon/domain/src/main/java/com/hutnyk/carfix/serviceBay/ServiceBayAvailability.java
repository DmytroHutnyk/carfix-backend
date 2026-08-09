package com.hutnyk.carfix.serviceBay;

import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ServiceBayAvailability {

    @EqualsAndHashCode.Include
    private final Integer id;

    private final TimeRange availableTime;
    private final LocalDate date;

    //Nullable — null for a one-off entry, set for every row of one recurrence series
    private final Integer seriesId;
    private final Integer serviceBayId;

    @Builder
    private ServiceBayAvailability(
            Integer id, TimeRange availableTime, LocalDate date, Integer seriesId, Integer serviceBayId) {
        this.id = id;
        this.availableTime = Validator.notNull(availableTime, "availableTime");
        this.date = Validator.notNull(date, "date");
        this.seriesId = seriesId;
        this.serviceBayId = Validator.notNull(serviceBayId, "serviceBayId");
    }

    public static ServiceBayAvailability of(
            Integer id, TimeRange availableTime, LocalDate date, Integer seriesId, Integer serviceBayId) {
        return ServiceBayAvailability.builder()
                .id(id)
                .availableTime(availableTime)
                .date(date)
                .seriesId(seriesId)
                .serviceBayId(serviceBayId)
                .build();
    }
}
