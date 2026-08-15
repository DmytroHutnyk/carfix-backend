package com.hutnyk.carfix.employee.repository;

import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    List<EmployeeEntity> findAllByBranchEntityId(UUID branchId);
}
