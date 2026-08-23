package com.hutnyk.carfix.openingHours.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursExceptionEntity;

public final class OpeningHoursMapper {

    private OpeningHoursMapper() {
    }

    public static OpeningHours toDomain(OpeningHoursEntity e) {
        if (e == null) return null;
        return OpeningHours.of(
                e.getId(),
                e.getDayOfWeek(),
                e.getStartTime(),
                e.getCloseTime(),
                e.getMode(),
                BranchId.of(e.getBranchEntity().getId()));
    }

    public static OpeningHoursException toDomain(OpeningHoursExceptionEntity e) {
        if (e == null) return null;
        return OpeningHoursException.of(
                e.getId(),
                e.getDate(),
                e.getStartTime(),
                e.getCloseTime(),
                e.getIsOpen(),
                e.getReason(),
                BranchId.of(e.getBranchEntity().getId()));
    }
}
