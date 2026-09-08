package com.hutnyk.carfix.employee.exception;

import com.hutnyk.carfix.exception.NotFoundException;

import java.util.UUID;

public class EmployeeNotFoundException extends NotFoundException {

    public EmployeeNotFoundException(UUID employeeId) {
        super(EmployeeErrorCode.EMPLOYEE_NOT_FOUND, "Employee", employeeId);
    }
}
