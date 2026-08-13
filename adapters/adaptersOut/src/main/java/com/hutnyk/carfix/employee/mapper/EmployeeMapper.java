package com.hutnyk.carfix.employee.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import com.hutnyk.carfix.user.mapper.UserMapper;

public class EmployeeMapper {

    public static Employee toDomain(EmployeeEntity e) {
        if (e == null) return null;
        return Employee.of(
                UserMapper.toDomain(e.getUserEntity()),
                e.getStatus(),
                e.getNotes(),
                e.getSalary(),
                BranchId.of(e.getBranchEntity().getId()));
    }
}
