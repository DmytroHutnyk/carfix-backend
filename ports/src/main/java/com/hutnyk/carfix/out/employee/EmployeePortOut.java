package com.hutnyk.carfix.out.employee;

import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.in.employee.query.OwnerEmployeeView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeePortOut {
    Employee insert(Employee employee);

    List<OwnerEmployeeView> findViewsByBranchId(UUID branchId);

    Optional<Employee> findByIdAndBranchId(UUID employeeId, UUID branchId);

    Employee update(Employee employee);
}
