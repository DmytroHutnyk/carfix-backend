package com.hutnyk.carfix.scheduling;

import java.util.List;

public record EquipmentSchedule(Integer equipmentId, Integer equipmentTypeId, List<TimeRange> free) {
    public EquipmentSchedule {
        free = List.copyOf(free);
    }
}
