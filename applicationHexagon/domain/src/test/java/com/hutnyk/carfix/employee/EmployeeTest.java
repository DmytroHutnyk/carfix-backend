package com.hutnyk.carfix.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class EmployeeTest {

    private static final BranchId BRANCH_ID = BranchId.of(UUID.randomUUID());

    private User user() {
        return User.of(
                UserId.of(UUID.randomUUID()),
                "Jan",
                "Kowalski",
                new PhoneNumber("+48", "123456789"),
                "jan@example.com",
                UserRole.EMPLOYEE,
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                LocalDate.of(1990, 1, 1),
                null,
                null);
    }

    @Test
    public void test_of_builds_employee() {
        //given
        User user = user();

        //when
        Employee result = Employee.of(user, EmployeeStatus.ACTIVE, "senior", new BigDecimal("8500.00"), BRANCH_ID);

        //then
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(result.getNotes()).isEqualTo("senior");
        assertThat(result.getSalary()).isEqualByComparingTo("8500.00");
        assertThat(result.getBranchId()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_of_throws_when_salary_is_negative() {
        //when + then
        assertThatThrownBy(() ->
                Employee.of(user(), EmployeeStatus.ACTIVE, null, new BigDecimal("-1.00"), BRANCH_ID))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.VALUE_OUT_OF_RANGE);
    }
}
