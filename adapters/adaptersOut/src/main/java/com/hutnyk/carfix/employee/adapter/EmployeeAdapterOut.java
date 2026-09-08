package com.hutnyk.carfix.employee.adapter;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import com.hutnyk.carfix.employee.mapper.EmployeeMapper;
import com.hutnyk.carfix.employee.repository.EmployeeRepository;
import com.hutnyk.carfix.in.employee.query.OwnerEmployeeView;
import com.hutnyk.carfix.out.employee.EmployeePortOut;
import com.hutnyk.carfix.role.entity.RoleEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@PersistenceAdapter
public class EmployeeAdapterOut implements EmployeePortOut {

    private final EmployeeRepository employeeRepository;
    private final EntityManager entityManager;

    @Override
    public Employee insert(Employee employee) {
        BranchEntity branch = entityManager.getReference(BranchEntity.class, employee.getBranchId().id());
        Set<RoleEntity> roles = roleReferences(employee);
        EmployeeEntity entity = EmployeeMapper.toEntity(employee, branch, roles);
        entityManager.persist(entity);
        return EmployeeMapper.toDomain(entity);
    }

    @Override
    public List<OwnerEmployeeView> findViewsByBranchId(UUID branchId) {
        return employeeRepository.findAllWithRolesByBranchId(branchId).stream()
                .map(EmployeeMapper::toOwnerView)
                .toList();
    }

    @Override
    public Optional<Employee> findByIdAndBranchId(UUID employeeId, UUID branchId) {
        return employeeRepository.findByIdAndBranchEntityId(employeeId, branchId).map(EmployeeMapper::toDomain);
    }

    @Override
    public Employee update(Employee employee) {
        EmployeeEntity entity = employeeRepository.findById(employee.getId().id())
                .orElseThrow(IllegalStateException::new);
        EmployeeMapper.updateEntity(entity, employee, roleReferences(employee));
        return EmployeeMapper.toDomain(employeeRepository.save(entity));
    }

    private Set<RoleEntity> roleReferences(Employee employee) {
        return employee.getRoleIds().stream()
                .map(id -> entityManager.getReference(RoleEntity.class, id))
                .collect(Collectors.toSet());
    }
}
