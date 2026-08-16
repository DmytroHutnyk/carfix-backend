package com.hutnyk.carfix.openingHours.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursExceptionEntity;

public final class OpeningHoursMapper {

    private OpeningHoursMapper() {
    }

    public static OpeningHours toDomain(OpeningHoursEntity entity) {
        if (entity == null) {
            return null;
        }
        return OpeningHours.of(
                entity.getId(),
                entity.getDayOfWeek(),
                entity.getStartTime(),
                entity.getCloseTime(),
                BranchId.of(entity.getBranchEntity().getId()));
    }

    public static OpeningHoursException toDomain(OpeningHoursExceptionEntity entity) {
        if (entity == null) {
            return null;
        }
        return OpeningHoursException.of(
                entity.getId(),
                entity.getDate(),
                entity.getStartTime(),
                entity.getCloseTime(),
                entity.getIsOpen(),
                entity.getReason(),
                BranchId.of(entity.getBranchEntity().getId()));
    }
}
