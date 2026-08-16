package com.hutnyk.carfix.employee.adapter;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import com.hutnyk.carfix.employee.mapper.EmployeeMapper;
import com.hutnyk.carfix.out.employee.EmployeePortOut;
import com.hutnyk.carfix.role.entity.RoleEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@PersistenceAdapter
public class EmployeeAdapterOut implements EmployeePortOut {

    private final EntityManager entityManager;

    @Override
    public Employee insert(Employee employee) {
        BranchEntity branch = entityManager.getReference(BranchEntity.class, employee.getBranchId().id());
        Set<RoleEntity> roles = employee.getRoleIds().stream()
                .map(id -> entityManager.getReference(RoleEntity.class, id))
                .collect(Collectors.toSet());
        EmployeeEntity entity = EmployeeMapper.toEntity(employee, branch, roles);
        /* app-minted UUID id: persist, not save — save() would merge and pay an extra SELECT */
        entityManager.persist(entity);
        return EmployeeMapper.toDomain(entity);
    }
}
