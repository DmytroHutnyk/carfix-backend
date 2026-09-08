package com.hutnyk.carfix.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.UserId;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class EmployeeTest {

    private static final BranchId BRANCH_ID = BranchId.of(UUID.randomUUID());
    private static final EmployeeId ID = EmployeeId.of(UUID.randomUUID());
    private static final EmployeeAddress ADDRESS =
            new EmployeeAddress("Pulawska", "12", "Masovian", "Poland", "02-515");

    @Test
    public void test_of_builds_employee_with_every_field() {
        UserId account = UserId.genId();

        //when
        Employee result = Employee.of(ID, "Jan", "Kowalski", "+48600100200", "jan@carfix.dev", account,
                EmployeeStatus.SUSPENDED, "senior", new BigDecimal("8500.00"), ADDRESS, BRANCH_ID, Set.of(1, 2));

        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getFirstName()).isEqualTo("Jan");
        assertThat(result.getLastName()).isEqualTo("Kowalski");
        assertThat(result.getPhone()).isEqualTo("+48600100200");
        assertThat(result.getEmail()).isEqualTo("jan@carfix.dev");
        assertThat(result.getUserId()).isEqualTo(account);
        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.SUSPENDED);
        assertThat(result.getNotes()).isEqualTo("senior");
        assertThat(result.getSalary()).isEqualByComparingTo("8500.00");
        assertThat(result.getAddress()).isEqualTo(ADDRESS);
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
        assertThat(result.getRoleIds()).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    public void test_create_is_active_without_account_salary_or_notes() {
        Employee result = Employee.create(ID, "Oleh", "Savchuk", BRANCH_ID, Set.of(7));

        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(result.getUserId()).isNull();
        assertThat(result.getSalary()).isNull();
        assertThat(result.getNotes()).isNull();
        assertThat(result.getPhone()).isNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getAddress()).isNull();
        assertThat(result.getRoleIds()).containsExactly(7);
    }

    @Test
    public void test_create_with_contact_block_is_active_and_carries_phone_email_and_address() {
        //when
        Employee result = Employee.create(ID, "Anna", "Nowak", "+48700200300", "anna@carfix.dev",
                new BigDecimal("6200.50"), ADDRESS, BRANCH_ID, Set.of(3));

        //then
        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(result.getUserId()).isNull();
        assertThat(result.getPhone()).isEqualTo("+48700200300");
        assertThat(result.getEmail()).isEqualTo("anna@carfix.dev");
        assertThat(result.getSalary()).isEqualByComparingTo("6200.50");
        assertThat(result.getAddress()).isEqualTo(ADDRESS);
        assertThat(result.getRoleIds()).containsExactly(3);
    }

    @Test
    public void test_update_preserves_id_status_account_and_branch() {
        //given
        UserId account = UserId.genId();
        Employee existing = Employee.of(ID, "Jan", "Kowalski", "+48600100200", "jan@carfix.dev", account,
                EmployeeStatus.SUSPENDED, "senior", new BigDecimal("8500.00"), ADDRESS, BRANCH_ID, Set.of(1));
        EmployeeAddress moved = new EmployeeAddress("Wolska", "3", "Masovian", "Poland", "01-001");

        //when
        Employee result = Employee.update(existing, "Janusz", "Nowak", "+48611222333", "janusz@carfix.dev",
                new BigDecimal("9000.00"), moved, Set.of(4, 5));

        //then
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.SUSPENDED);
        assertThat(result.getUserId()).isEqualTo(account);
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
        assertThat(result.getFirstName()).isEqualTo("Janusz");
        assertThat(result.getLastName()).isEqualTo("Nowak");
        assertThat(result.getPhone()).isEqualTo("+48611222333");
        assertThat(result.getEmail()).isEqualTo("janusz@carfix.dev");
        assertThat(result.getSalary()).isEqualByComparingTo("9000.00");
        assertThat(result.getAddress()).isEqualTo(moved);
        assertThat(result.getRoleIds()).containsExactlyInAnyOrder(4, 5);
    }

    @Test
    public void test_of_throws_when_salary_is_negative() {
        //when + then
        assertThatThrownBy(() -> Employee.of(ID, "Jan", "Kowalski", null, null, null, EmployeeStatus.ACTIVE, null,
                new BigDecimal("-1.00"), null, BRANCH_ID, Set.of()))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.VALUE_OUT_OF_RANGE);
    }

    @Test
    public void test_names_and_identity_are_mandatory() {
        assertThatThrownBy(() -> Employee.create(ID, " ", "Kowalski", BRANCH_ID, Set.of(1)))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType", "fieldName")
                .containsExactly(ValidationErrorType.EMPTY_STRING, "firstName");
        assertThatThrownBy(() -> Employee.create(ID, "Jan", null, BRANCH_ID, Set.of(1)))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("fieldName")
                .isEqualTo("lastName");
        assertThatThrownBy(() -> Employee.create(null, "Jan", "Kowalski", BRANCH_ID, Set.of(1)))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("fieldName")
                .isEqualTo("id");
        assertThatThrownBy(() -> Employee.create(ID, "Jan", "Kowalski", BRANCH_ID, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("fieldName")
                .isEqualTo("roleIds");
    }

    @Test
    public void test_role_ids_are_defensively_copied() {
        java.util.Set<Integer> mutable = new java.util.HashSet<>(Set.of(1));

        Employee result = Employee.create(ID, "Jan", "Kowalski", BRANCH_ID, mutable);
        mutable.add(2);

        assertThat(result.getRoleIds()).containsExactly(1);
    }
}
