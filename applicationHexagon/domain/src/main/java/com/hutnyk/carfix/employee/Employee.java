package com.hutnyk.carfix.employee;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Employee {

    @EqualsAndHashCode.Include
    private final User user;
    private final EmployeeStatus status;

    //Nullable
    private final String notes;
    private final BigDecimal salary;
    private final BranchId branchId;

    @Builder
    private Employee(User user, EmployeeStatus status, String notes, BigDecimal salary, BranchId branchId) {
        this.user = Validator.notNull(user, "user");
        this.status = Validator.notNull(status, "status");
        this.notes = notes;
        this.salary = validateSalary(salary);
        this.branchId = Validator.notNull(branchId, "branchId");
    }

    public static Employee of(User user, EmployeeStatus status, String notes, BigDecimal salary, BranchId branchId) {
        return Employee.builder()
                .user(user)
                .status(status)
                .notes(notes)
                .salary(salary)
                .branchId(branchId)
                .build();
    }

    private static BigDecimal validateSalary(BigDecimal salary) {
        Validator.notNull(salary, "salary");

        if (salary.signum() < 0) {
            throw new DomainObjectValidationException(ValidationErrorType.VALUE_OUT_OF_RANGE, "salary", salary);
        }

        return salary;
    }
}
