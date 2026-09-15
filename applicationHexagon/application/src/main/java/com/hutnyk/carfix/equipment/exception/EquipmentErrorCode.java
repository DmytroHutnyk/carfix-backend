package com.hutnyk.carfix.equipment.exception;

import com.hutnyk.carfix.exception.ErrorCategory;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EquipmentErrorCode implements ErrorCode {

    EQUIPMENT_NOT_FOUND(ErrorCategory.NOT_FOUND);

    private final ErrorCategory category;

    @Override
    public String code() {
        return name();
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}
