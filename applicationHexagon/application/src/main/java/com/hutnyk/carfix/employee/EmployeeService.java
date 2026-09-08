package com.hutnyk.carfix.employee;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.employee.exception.EmployeeNotFoundException;
import com.hutnyk.carfix.in.employee.OwnerEmployeePortIn;
import com.hutnyk.carfix.in.employee.commands.CreateEmployeeCommand;
import com.hutnyk.carfix.in.employee.commands.UpdateEmployeeCommand;
import com.hutnyk.carfix.in.employee.query.OwnerEmployeeView;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.employee.EmployeePortOut;
import com.hutnyk.carfix.out.role.RolePortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.role.Role;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@ApplicationService
public class EmployeeService implements OwnerEmployeePortIn {

    private final EmployeePortOut employeePortOut;
    private final RolePortOut rolePortOut;
    private final OwnerBranchPortOut ownerBranchPortOut;
    private final UserPortOut userPortOut;

    @Override
    @Transactional(readOnly = true)
    public List<OwnerEmployeeView> getEmployees(String ownerEmail, UUID branchId) {
        requireOwnedBranch(ownerEmail, branchId);
        return employeePortOut.findViewsByBranchId(branchId);
    }

    @Override
    public OwnerEmployeeView createEmployee(String ownerEmail, UUID branchId, CreateEmployeeCommand command) {
        requireOwnedBranch(ownerEmail, branchId);
        BranchId id = BranchId.of(branchId);
        Set<Integer> roleIds = resolveRoleIds(id, command.roleNames());
        Employee employee = Employee.create(EmployeeId.genId(), command.name().trim(), command.surname().trim(),
                command.phone().trim(), command.email().trim(), command.salary(), address(command), id, roleIds);
        Employee inserted = employeePortOut.insert(employee);
        return readView(branchId, inserted.getId().id());
    }

    @Override
    public OwnerEmployeeView updateEmployee(String ownerEmail, UUID branchId, UUID employeeId, UpdateEmployeeCommand command) {
        requireOwnedBranch(ownerEmail, branchId);
        Employee existing = employeePortOut.findByIdAndBranchId(employeeId, branchId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        Set<Integer> roleIds = resolveRoleIds(BranchId.of(branchId), command.roleNames());
        Employee rebuilt = Employee.update(existing, command.name().trim(), command.surname().trim(),
                command.phone().trim(), command.email().trim(), command.salary(), address(command), roleIds);
        employeePortOut.update(rebuilt);
        return readView(branchId, employeeId);
    }

    private void requireOwnedBranch(String ownerEmail, UUID branchId) {
        User owner = userPortOut.loadUserByEmail(ownerEmail)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(ownerEmail));
        ownerBranchPortOut.findDetailByIdAndOwnerId(branchId, owner.getId())
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    private Set<Integer> resolveRoleIds(BranchId branchId, List<String> roleNames) {
        Map<String, Integer> byName = new HashMap<>();
        for (Role role : rolePortOut.findAllForBranch(branchId.id())) {
            byName.putIfAbsent(key(role.getName()), role.getId());
        }
        Set<Integer> ids = new LinkedHashSet<>();
        for (String name : roleNames) {
            String trimmed = name.trim();
            Integer roleId = byName.get(key(trimmed));
            if (roleId == null) {
                roleId = rolePortOut.insert(Role.create(trimmed, branchId)).getId();
                byName.put(key(trimmed), roleId);
            }
            ids.add(roleId);
        }
        return ids;
    }

    private OwnerEmployeeView readView(UUID branchId, UUID employeeId) {
        return employeePortOut.findViewsByBranchId(branchId).stream()
                .filter(view -> view.id().equals(employeeId))
                .findFirst()
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    private static EmployeeAddress address(CreateEmployeeCommand command) {
        return new EmployeeAddress(blankToNull(command.street()), blankToNull(command.apartment()),
                blankToNull(command.region()), blankToNull(command.country()), blankToNull(command.postalCode()));
    }

    private static EmployeeAddress address(UpdateEmployeeCommand command) {
        return new EmployeeAddress(blankToNull(command.street()), blankToNull(command.apartment()),
                blankToNull(command.region()), blankToNull(command.country()), blankToNull(command.postalCode()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
