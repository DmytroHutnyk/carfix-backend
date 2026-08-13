package com.hutnyk.carfix.equipment;

import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EquipmentAvailability {

    @EqualsAndHashCode.Include
    private final Integer id;

    private final TimeRange availableTime;
    private final LocalDate date;

    //Nullable — null for a one-off entry, set for every row of one recurrence series
    private final Integer seriesId;
    private final Integer equipmentId;

    @Builder
    private EquipmentAvailability(
            Integer id, TimeRange availableTime, LocalDate date, Integer seriesId, Integer equipmentId) {
        this.id = id;
        this.availableTime = Validator.notNull(availableTime, "availableTime");
        this.date = Validator.notNull(date, "date");
        this.seriesId = seriesId;
        this.equipmentId = Validator.notNull(equipmentId, "equipmentId");
    }

    public static EquipmentAvailability of(
            Integer id, TimeRange availableTime, LocalDate date, Integer seriesId, Integer equipmentId) {
        return EquipmentAvailability.builder()
                .id(id)
                .availableTime(availableTime)
                .date(date)
                .seriesId(seriesId)
                .equipmentId(equipmentId)
                .build();
    }
}
