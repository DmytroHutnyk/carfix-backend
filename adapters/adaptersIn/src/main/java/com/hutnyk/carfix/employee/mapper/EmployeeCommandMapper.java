package com.hutnyk.carfix.employee.mapper;

import com.hutnyk.carfix.employee.dto.request.CreateEmployeeRequest;
import com.hutnyk.carfix.employee.dto.request.EmployeeAddressRequest;
import com.hutnyk.carfix.employee.dto.request.UpdateEmployeeRequest;
import com.hutnyk.carfix.in.employee.commands.CreateEmployeeCommand;
import com.hutnyk.carfix.in.employee.commands.UpdateEmployeeCommand;

import java.util.List;

public final class EmployeeCommandMapper {

    private EmployeeCommandMapper() {
    }

    public static CreateEmployeeCommand toCreateCommand(CreateEmployeeRequest r) {
        if (r == null) {
            return null;
        }
        EmployeeAddressRequest a = r.address();
        return new CreateEmployeeCommand(r.name(), r.surname(), r.phone(), r.email(), r.salary(),
                List.copyOf(r.roles()), a.street(), a.apartment(), a.region(), a.country(), a.postalCode());
    }

    public static UpdateEmployeeCommand toUpdateCommand(UpdateEmployeeRequest r) {
        if (r == null) {
            return null;
        }
        EmployeeAddressRequest a = r.address();
        return new UpdateEmployeeCommand(r.name(), r.surname(), r.phone(), r.email(), r.salary(),
                List.copyOf(r.roles()), a.street(), a.apartment(), a.region(), a.country(), a.postalCode());
    }
}
