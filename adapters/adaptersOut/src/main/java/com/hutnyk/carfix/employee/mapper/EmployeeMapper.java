package com.hutnyk.carfix.employee.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import com.hutnyk.carfix.role.entity.RoleEntity;
import com.hutnyk.carfix.user.UserId;

import java.util.Set;
import java.util.stream.Collectors;

public class EmployeeMapper {

    public static Employee toDomain(EmployeeEntity e) {
        if (e == null) return null;
        return Employee.of(
                EmployeeId.of(e.getId()),
                e.getFirstName(),
                e.getLastName(),
                e.getUserId() == null ? null : UserId.of(e.getUserId()),
                e.getStatus(),
                e.getNotes(),
                e.getSalary(),
                BranchId.of(e.getBranchEntity().getId()),
                e.getRoles() == null
                        ? Set.of()
                        : e.getRoles().stream().map(RoleEntity::getId).collect(Collectors.toSet()));
    }

    public static EmployeeEntity toEntity(Employee employee, BranchEntity branch, Set<RoleEntity> roles) {
        if (employee == null) return null;
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(employee.getId().id());
        entity.setFirstName(employee.getFirstName());
        entity.setLastName(employee.getLastName());
        entity.setUserId(employee.getUserId() == null ? null : employee.getUserId().id());
        entity.setStatus(employee.getStatus());
        entity.setNotes(employee.getNotes());
        entity.setSalary(employee.getSalary());
        entity.setBranchEntity(branch);
        entity.setRoles(roles);
        return entity;
    }
}
