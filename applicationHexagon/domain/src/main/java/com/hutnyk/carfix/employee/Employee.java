package com.hutnyk.carfix.employee;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Employee {

    @EqualsAndHashCode.Include
    private final EmployeeId id;
    private final String firstName;
    private final String lastName;

    //Nullable — the login account this staff member holds, if any
    private final UserId userId;
    private final EmployeeStatus status;

    //Nullable
    private final String notes;
    //Nullable
    private final BigDecimal salary;
    private final BranchId branchId;
    private final Set<Integer> roleIds;

    @Builder
    private Employee(
            EmployeeId id,
            String firstName,
            String lastName,
            UserId userId,
            EmployeeStatus status,
            String notes,
            BigDecimal salary,
            BranchId branchId,
            Set<Integer> roleIds) {
        this.id = Validator.notNull(id, "id");
        this.firstName = Validator.notBlank(firstName, "firstName");
        this.lastName = Validator.notBlank(lastName, "lastName");
        this.userId = userId;
        this.status = Validator.notNull(status, "status");
        this.notes = notes;
        this.salary = validateSalary(salary);
        this.branchId = Validator.notNull(branchId, "branchId");
        this.roleIds = Set.copyOf(Validator.notNull(roleIds, "roleIds"));
    }

    public static Employee of(
            EmployeeId id,
            String firstName,
            String lastName,
            UserId userId,
            EmployeeStatus status,
            String notes,
            BigDecimal salary,
            BranchId branchId,
            Set<Integer> roleIds) {
        return Employee.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .userId(userId)
                .status(status)
                .notes(notes)
                .salary(salary)
                .branchId(branchId)
                .roleIds(roleIds)
                .build();
    }

    public static Employee create(EmployeeId id, String firstName, String lastName, BranchId branchId, Set<Integer> roleIds) {
        return of(id, firstName, lastName, null, EmployeeStatus.ACTIVE, null, null, branchId, roleIds);
    }

    private static BigDecimal validateSalary(BigDecimal salary) {
        if (salary == null) {
            return null;
        }
        if (salary.signum() < 0) {
            throw new DomainObjectValidationException(ValidationErrorType.VALUE_OUT_OF_RANGE, "salary", salary);
        }
        return salary;
    }
}
