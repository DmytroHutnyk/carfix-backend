package com.hutnyk.carfix.employee.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.employee.EmployeeStatus;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import com.hutnyk.carfix.role.entity.RoleEntity;
import com.hutnyk.carfix.user.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public class EmployeeMapperTest {

    private static final UUID BRANCH_UUID = UUID.randomUUID();

    private static BranchEntity branch() {
        BranchEntity branch = new BranchEntity();
        branch.setId(BRANCH_UUID);
        return branch;
    }

    private static RoleEntity role(int id) {
        RoleEntity role = new RoleEntity();
        role.setId(id);
        role.setName("Role " + id);
        return role;
    }

    @Test
    public void test_toEntity_copies_fields_and_role_references() {
        Employee employee = Employee.create(EmployeeId.genId(), "Oleh", "Savchuk", BranchId.of(BRANCH_UUID), Set.of(3, 5));

        EmployeeEntity entity = EmployeeMapper.toEntity(employee, branch(), Set.of(role(3), role(5)));

        assertThat(entity.getId()).isEqualTo(employee.getId().id());
        assertThat(entity.getFirstName()).isEqualTo("Oleh");
        assertThat(entity.getLastName()).isEqualTo("Savchuk");
        assertThat(entity.getUserId()).isNull();
        assertThat(entity.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(entity.getSalary()).isNull();
        assertThat(entity.getBranchEntity().getId()).isEqualTo(BRANCH_UUID);
        assertThat(entity.getRoles()).extracting(RoleEntity::getId).containsExactlyInAnyOrder(3, 5);
    }

    @Test
    public void test_toDomain_reads_account_link_and_roles() {
        UUID account = UUID.randomUUID();
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(UUID.randomUUID());
        entity.setFirstName("Adam");
        entity.setLastName("Nowak");
        entity.setUserId(account);
        entity.setStatus(EmployeeStatus.SUSPENDED);
        entity.setNotes("n");
        entity.setSalary(new BigDecimal("100.00"));
        entity.setBranchEntity(branch());
        entity.setRoles(Set.of(role(1)));

        Employee employee = EmployeeMapper.toDomain(entity);

        assertThat(employee.getId()).isEqualTo(EmployeeId.of(entity.getId()));
        assertThat(employee.getUserId()).isEqualTo(UserId.of(account));
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.SUSPENDED);
        assertThat(employee.getSalary()).isEqualByComparingTo("100.00");
        assertThat(employee.getBranchId()).isEqualTo(BranchId.of(BRANCH_UUID));
        assertThat(employee.getRoleIds()).containsExactly(1);
    }

    @Test
    public void test_toDomain_tolerates_missing_roles_collection() {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(UUID.randomUUID());
        entity.setFirstName("Adam");
        entity.setLastName("Nowak");
        entity.setStatus(EmployeeStatus.ACTIVE);
        entity.setBranchEntity(branch());

        assertThat(EmployeeMapper.toDomain(entity).getRoleIds()).isEmpty();
        assertThat(EmployeeMapper.toDomain((EmployeeEntity) null)).isNull();
        assertThat(EmployeeMapper.toEntity((Employee) null, null, null)).isNull();
    }
}
