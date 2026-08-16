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

    @Test
    public void test_of_builds_employee_with_every_field() {
        //given
        UserId account = UserId.genId();

        //when
        Employee result = Employee.of(ID, "Jan", "Kowalski", account, EmployeeStatus.SUSPENDED,
                "senior", new BigDecimal("8500.00"), BRANCH_ID, Set.of(1, 2));

        //then
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getFirstName()).isEqualTo("Jan");
        assertThat(result.getLastName()).isEqualTo("Kowalski");
        assertThat(result.getUserId()).isEqualTo(account);
        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.SUSPENDED);
        assertThat(result.getNotes()).isEqualTo("senior");
        assertThat(result.getSalary()).isEqualByComparingTo("8500.00");
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
        assertThat(result.getRoleIds()).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    public void test_create_is_active_without_account_salary_or_notes() {
        //when
        Employee result = Employee.create(ID, "Oleh", "Savchuk", BRANCH_ID, Set.of(7));

        //then
        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(result.getUserId()).isNull();
        assertThat(result.getSalary()).isNull();
        assertThat(result.getNotes()).isNull();
        assertThat(result.getRoleIds()).containsExactly(7);
    }

    @Test
    public void test_of_throws_when_salary_is_negative() {
        //when + then
        assertThatThrownBy(() -> Employee.of(ID, "Jan", "Kowalski", null, EmployeeStatus.ACTIVE, null,
                new BigDecimal("-1.00"), BRANCH_ID, Set.of()))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.VALUE_OUT_OF_RANGE);
    }

    @Test
    public void test_names_and_identity_are_mandatory() {
        //when + then
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
        //given
        java.util.Set<Integer> mutable = new java.util.HashSet<>(Set.of(1));

        //when
        Employee result = Employee.create(ID, "Jan", "Kowalski", BRANCH_ID, mutable);
        mutable.add(2);

        //then
        assertThat(result.getRoleIds()).containsExactly(1);
    }
}
