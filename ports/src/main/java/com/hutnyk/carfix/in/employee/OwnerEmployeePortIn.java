package com.hutnyk.carfix.in.employee;

import com.hutnyk.carfix.in.employee.commands.CreateEmployeeCommand;
import com.hutnyk.carfix.in.employee.commands.UpdateEmployeeCommand;
import com.hutnyk.carfix.in.employee.query.OwnerEmployeeView;

import java.util.List;
import java.util.UUID;

public interface OwnerEmployeePortIn {

    List<OwnerEmployeeView> getEmployees(String ownerEmail, UUID branchId);

    OwnerEmployeeView createEmployee(String ownerEmail, UUID branchId, CreateEmployeeCommand command);

    OwnerEmployeeView updateEmployee(String ownerEmail, UUID branchId, UUID employeeId, UpdateEmployeeCommand command);
}
