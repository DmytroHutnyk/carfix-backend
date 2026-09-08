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

    //Nullable
    private final String phone;
    //Nullable
    private final String email;

    //Nullable
    private final UserId userId;
    private final EmployeeStatus status;

    //Nullable
    private final String notes;
    //Nullable
    private final BigDecimal salary;

    //Nullable
    private final EmployeeAddress address;
    private final BranchId branchId;
    private final Set<Integer> roleIds;

    @Builder
    private Employee(
            EmployeeId id,
            String firstName,
            String lastName,
            String phone,
            String email,
            UserId userId,
            EmployeeStatus status,
            String notes,
            BigDecimal salary,
            EmployeeAddress address,
            BranchId branchId,
            Set<Integer> roleIds) {
        this.id = Validator.notNull(id, "id");
        this.firstName = Validator.notBlank(firstName, "firstName");
        this.lastName = Validator.notBlank(lastName, "lastName");
        this.phone = phone;
        this.email = email;
        this.userId = userId;
        this.status = Validator.notNull(status, "status");
        this.notes = notes;
        this.salary = validateSalary(salary);
        this.address = address;
        this.branchId = Validator.notNull(branchId, "branchId");
        this.roleIds = Set.copyOf(Validator.notNull(roleIds, "roleIds"));
    }

    public static Employee of(
            EmployeeId id,
            String firstName,
            String lastName,
            String phone,
            String email,
            UserId userId,
            EmployeeStatus status,
            String notes,
            BigDecimal salary,
            EmployeeAddress address,
            BranchId branchId,
            Set<Integer> roleIds) {
        return Employee.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .email(email)
                .userId(userId)
                .status(status)
                .notes(notes)
                .salary(salary)
                .address(address)
                .branchId(branchId)
                .roleIds(roleIds)
                .build();
    }

    public static Employee create(EmployeeId id, String firstName, String lastName, BranchId branchId, Set<Integer> roleIds) {
        return of(id, firstName, lastName, null, null, null, EmployeeStatus.ACTIVE, null, null, null, branchId, roleIds);
    }

    public static Employee create(EmployeeId id, String firstName, String lastName, String phone, String email,
                                  BigDecimal salary, EmployeeAddress address, BranchId branchId, Set<Integer> roleIds) {
        return of(id, firstName, lastName, phone, email, null, EmployeeStatus.ACTIVE, null, salary, address, branchId, roleIds);
    }

    public static Employee update(Employee existing, String firstName, String lastName, String phone, String email,
                                  BigDecimal salary, EmployeeAddress address, Set<Integer> roleIds) {
        return of(existing.id, firstName, lastName, phone, email, existing.userId, existing.status, existing.notes,
                salary, address, existing.branchId, roleIds);
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
