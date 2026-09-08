package com.hutnyk.carfix.employee.mapper;

import com.hutnyk.carfix.employee.dto.response.EmployeeAddressResponse;
import com.hutnyk.carfix.employee.dto.response.EmployeeResponse;
import com.hutnyk.carfix.in.employee.query.OwnerEmployeeView;

import java.math.BigDecimal;

public final class EmployeeResponseMapper {

    private EmployeeResponseMapper() {
    }

    public static EmployeeResponse toResponse(OwnerEmployeeView view) {
        if (view == null) {
            return null;
        }
        return new EmployeeResponse(
                view.id().toString(),
                view.firstName(),
                view.lastName(),
                orEmpty(view.phone()),
                orEmpty(view.email()),
                view.salary() == null ? BigDecimal.ZERO : view.salary(),
                view.roleNames(),
                new EmployeeAddressResponse(
                        orEmpty(view.street()),
                        orEmpty(view.apartment()),
                        orEmpty(view.region()),
                        orEmpty(view.country()),
                        orEmpty(view.postalCode())),
                view.status() == null ? null : view.status().name());
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
