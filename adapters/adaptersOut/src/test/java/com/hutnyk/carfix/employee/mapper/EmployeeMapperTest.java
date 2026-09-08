package com.hutnyk.carfix.employee.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.employee.EmployeeAddress;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.employee.EmployeeStatus;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import com.hutnyk.carfix.in.employee.query.OwnerEmployeeView;
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

    @Test
    public void test_updateEntity_overwrites_editable_fields_and_replaces_roles() {
        //given
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(UUID.randomUUID());
        entity.setFirstName("Old");
        entity.setLastName("Name");
        entity.setPhone("+48000000000");
        entity.setStatus(EmployeeStatus.ACTIVE);
        entity.setBranchEntity(branch());
        entity.setRoles(Set.of(role(1)));
        Employee employee = Employee.create(EmployeeId.of(entity.getId()), "Anna", "Nowak", "+48700200300",
                "anna@carfix.dev", new BigDecimal("6200.50"),
                new EmployeeAddress("Wolska", "3", "Masovian", "Poland", "01-001"),
                BranchId.of(BRANCH_UUID), Set.of(2, 3));

        //when
        EmployeeMapper.updateEntity(entity, employee, Set.of(role(2), role(3)));

        //then
        assertThat(entity.getFirstName()).isEqualTo("Anna");
        assertThat(entity.getLastName()).isEqualTo("Nowak");
        assertThat(entity.getPhone()).isEqualTo("+48700200300");
        assertThat(entity.getEmail()).isEqualTo("anna@carfix.dev");
        assertThat(entity.getSalary()).isEqualByComparingTo("6200.50");
        assertThat(entity.getStreet()).isEqualTo("Wolska");
        assertThat(entity.getPostalCode()).isEqualTo("01-001");
        assertThat(entity.getRoles()).extracting(RoleEntity::getId).containsExactlyInAnyOrder(2, 3);
    }

    @Test
    public void test_toOwnerView_flattens_contact_address_and_role_names() {
        //given
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(UUID.randomUUID());
        entity.setFirstName("Anna");
        entity.setLastName("Nowak");
        entity.setPhone("+48700200300");
        entity.setEmail("anna@carfix.dev");
        entity.setSalary(new BigDecimal("6200.50"));
        entity.setStatus(EmployeeStatus.SUSPENDED);
        entity.setStreet("Wolska");
        entity.setApartment("3");
        entity.setRegion("Masovian");
        entity.setCountry("Poland");
        entity.setPostalCode("01-001");
        entity.setBranchEntity(branch());
        entity.setRoles(Set.of(role(1)));

        //when
        OwnerEmployeeView view = EmployeeMapper.toOwnerView(entity);

        //then
        assertThat(view.id()).isEqualTo(entity.getId());
        assertThat(view.firstName()).isEqualTo("Anna");
        assertThat(view.lastName()).isEqualTo("Nowak");
        assertThat(view.phone()).isEqualTo("+48700200300");
        assertThat(view.email()).isEqualTo("anna@carfix.dev");
        assertThat(view.salary()).isEqualByComparingTo("6200.50");
        assertThat(view.status()).isEqualTo(EmployeeStatus.SUSPENDED);
        assertThat(view.street()).isEqualTo("Wolska");
        assertThat(view.apartment()).isEqualTo("3");
        assertThat(view.region()).isEqualTo("Masovian");
        assertThat(view.country()).isEqualTo("Poland");
        assertThat(view.postalCode()).isEqualTo("01-001");
        assertThat(view.roleNames()).containsExactly("Role 1");
        assertThat(EmployeeMapper.toOwnerView(null)).isNull();
    }
}
