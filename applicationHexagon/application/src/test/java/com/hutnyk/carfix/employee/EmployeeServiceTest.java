package com.hutnyk.carfix.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.CancellationPolicy;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.employee.exception.EmployeeNotFoundException;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchDetailView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.in.employee.commands.CreateEmployeeCommand;
import com.hutnyk.carfix.in.employee.commands.UpdateEmployeeCommand;
import com.hutnyk.carfix.in.employee.query.OwnerEmployeeView;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.employee.EmployeePortOut;
import com.hutnyk.carfix.out.role.RolePortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.role.Role;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmployeeServiceTest {

    private static final String OWNER_EMAIL = "owner@carfix.dev";
    private static final UserId OWNER_ID = UserId.genId();
    private static final UUID BRANCH_ID = UUID.randomUUID();
    private static final com.hutnyk.carfix.branch.BranchId BRANCH = com.hutnyk.carfix.branch.BranchId.of(BRANCH_ID);

    private static User ownerUser() {
        return User.builder()
                .id(OWNER_ID)
                .name("Marek")
                .surname("Kowalski")
                .phoneNumber(new PhoneNumber("+48", "600100200"))
                .email(OWNER_EMAIL)
                .role(UserRole.OWNER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .build();
    }

    private static OwnerBranchDetailView detail() {
        return new OwnerBranchDetailView(
                BRANCH_ID, "AutoFix Mokotow", BranchStatus.ACTIVE,
                "A workshop.", CancellationPolicy.MODERATE,
                "+48221234567", "contact@autofix.pl", "Europe/Warsaw",
                new AddressView(55, "Pulawska", "45", null, "02-515", "Warsaw", "Masovian Voivodeship",
                        CountryIso.PL, "Poland", new BigDecimal("52.180000"), new BigDecimal("21.020000"), "ChIJx"),
                List.of(), List.of(), List.of());
    }

    private static CreateEmployeeCommand createCommand(List<String> roleNames) {
        return new CreateEmployeeCommand("Anna", "Nowak", "+48700200300", "anna@carfix.dev",
                new BigDecimal("6200.50"), roleNames, "Wolska", "3", "Masovian", "Poland", "01-001");
    }

    private static UpdateEmployeeCommand updateCommand(List<String> roleNames) {
        return new UpdateEmployeeCommand("Anna", "Nowak", "+48700200300", "anna@carfix.dev",
                new BigDecimal("6200.50"), roleNames, "Wolska", "3", "Masovian", "Poland", "01-001");
    }

    private static final class FakeUserPortOut implements UserPortOut {
        User user;

        @Override public Optional<User> loadUserByEmail(String email) { return Optional.ofNullable(user); }
        @Override public boolean existsByEmail(String email) { throw new UnsupportedOperationException(); }
        @Override public boolean existsByPhoneNumber(PhoneNumber phoneNumber) { throw new UnsupportedOperationException(); }
        @Override public User update(User user) { throw new UnsupportedOperationException(); }
    }

    private static final class FakeOwnerBranchPortOut implements OwnerBranchPortOut {
        OwnerBranchDetailView detail;

        @Override public List<OwnerBranchSummaryView> findSummariesByOwnerId(UserId ownerId, Instant now) { throw new UnsupportedOperationException(); }
        @Override public Optional<OwnerBranchDetailView> findDetailByIdAndOwnerId(UUID branchId, UserId ownerId) { return Optional.ofNullable(detail); }
    }

    private static final class FakeRolePortOut implements RolePortOut {
        final List<Role> roles = new ArrayList<>();
        final List<Role> inserted = new ArrayList<>();
        int nextId = 900;

        @Override
        public Role insert(Role role) {
            Role saved = Role.of(nextId++, role.getName(), role.getBranchId());
            roles.add(saved);
            inserted.add(saved);
            return saved;
        }

        @Override
        public List<Role> findAllForBranch(UUID branchId) {
            return roles.stream()
                    .filter(r -> r.getBranchId() == null || r.getBranchId().id().equals(branchId))
                    .toList();
        }
    }

    private static final class FakeEmployeePortOut implements EmployeePortOut {
        final List<Employee> inserted = new ArrayList<>();
        final List<Employee> updated = new ArrayList<>();
        Employee existing;
        UUID viewsBranchId;

        @Override public Employee insert(Employee employee) { inserted.add(employee); return employee; }
        @Override public Employee update(Employee employee) { updated.add(employee); return employee; }
        @Override public Optional<Employee> findByIdAndBranchId(UUID employeeId, UUID branchId) { return Optional.ofNullable(existing); }

        @Override
        public List<OwnerEmployeeView> findViewsByBranchId(UUID branchId) {
            viewsBranchId = branchId;
            List<Employee> all = new ArrayList<>(inserted);
            all.addAll(updated);
            if (existing != null) {
                all.add(existing);
            }
            return all.stream().map(FakeEmployeePortOut::toView).toList();
        }

        private static OwnerEmployeeView toView(Employee e) {
            EmployeeAddress a = e.getAddress();
            return new OwnerEmployeeView(e.getId().id(), e.getFirstName(), e.getLastName(), e.getPhone(), e.getEmail(),
                    e.getSalary(), e.getStatus(),
                    a == null ? null : a.street(), a == null ? null : a.apartment(), a == null ? null : a.region(),
                    a == null ? null : a.country(), a == null ? null : a.postalCode(), List.of());
        }
    }

    private final FakeUserPortOut userStub = new FakeUserPortOut();
    private final FakeOwnerBranchPortOut branchStub = new FakeOwnerBranchPortOut();
    private final FakeRolePortOut roleStub = new FakeRolePortOut();
    private final FakeEmployeePortOut employeeStub = new FakeEmployeePortOut();

    private final EmployeeService service = new EmployeeService(employeeStub, roleStub, branchStub, userStub);

    private void owned() {
        userStub.user = ownerUser();
        branchStub.detail = detail();
    }

    @Test
    public void test_create_reuses_matching_role_case_insensitively_and_creates_missing_ones() {
        //given
        owned();
        roleStub.roles.add(Role.of(1, "Mechanic", null));
        roleStub.roles.add(Role.of(2, "Painter", BRANCH));

        //when
        OwnerEmployeeView result = service.createEmployee(OWNER_EMAIL, BRANCH_ID, createCommand(List.of("mechanic", "Welder")));

        //then
        assertThat(employeeStub.inserted).singleElement().satisfies(e -> {
            assertThat(e.getFirstName()).isEqualTo("Anna");
            assertThat(e.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
            assertThat(e.getRoleIds()).containsExactlyInAnyOrder(1, 900);
        });
        assertThat(roleStub.inserted).singleElement().satisfies(r -> {
            assertThat(r.getName()).isEqualTo("Welder");
            assertThat(r.getBranchId()).isEqualTo(BRANCH);
        });
        assertThat(result.id()).isEqualTo(employeeStub.inserted.get(0).getId().id());
        assertThat(result.firstName()).isEqualTo("Anna");
    }

    @Test
    public void test_update_preserves_status_and_rebuilds_roles() {
        //given
        owned();
        roleStub.roles.add(Role.of(1, "Mechanic", null));
        UUID employeeId = UUID.randomUUID();
        employeeStub.existing = Employee.of(EmployeeId.of(employeeId), "Old", "Name", "+48000000000", "old@carfix.dev",
                UserId.genId(), EmployeeStatus.SUSPENDED, "senior", new BigDecimal("5000.00"), null, BRANCH, java.util.Set.of(9));

        //when
        OwnerEmployeeView result = service.updateEmployee(OWNER_EMAIL, BRANCH_ID, employeeId, updateCommand(List.of("Mechanic")));

        //then
        assertThat(employeeStub.updated).singleElement().satisfies(e -> {
            assertThat(e.getId().id()).isEqualTo(employeeId);
            assertThat(e.getStatus()).isEqualTo(EmployeeStatus.SUSPENDED);
            assertThat(e.getFirstName()).isEqualTo("Anna");
            assertThat(e.getRoleIds()).containsExactly(1);
        });
        assertThat(result.status()).isEqualTo(EmployeeStatus.SUSPENDED);
    }

    @Test
    public void test_create_on_foreign_or_unknown_branch_is_not_found_and_writes_nothing() {
        //given
        userStub.user = ownerUser();
        branchStub.detail = null;

        //when + then
        assertThatThrownBy(() -> service.createEmployee(OWNER_EMAIL, BRANCH_ID, createCommand(List.of("Mechanic"))))
                .isInstanceOf(BranchNotFoundException.class);
        assertThat(employeeStub.inserted).isEmpty();
        assertThat(roleStub.inserted).isEmpty();
    }

    @Test
    public void test_get_on_foreign_or_unknown_branch_is_not_found() {
        //given
        userStub.user = ownerUser();
        branchStub.detail = null;

        //when + then
        assertThatThrownBy(() -> service.getEmployees(OWNER_EMAIL, BRANCH_ID))
                .isInstanceOf(BranchNotFoundException.class);
    }

    @Test
    public void test_update_of_unknown_employee_is_not_found_and_writes_nothing() {
        //given
        owned();
        employeeStub.existing = null;

        //when + then
        assertThatThrownBy(() -> service.updateEmployee(OWNER_EMAIL, BRANCH_ID, UUID.randomUUID(), updateCommand(List.of("Mechanic"))))
                .isInstanceOf(EmployeeNotFoundException.class);
        assertThat(employeeStub.updated).isEmpty();
    }

    @Test
    public void test_missing_principal_row_is_an_authentication_failure() {
        //given
        userStub.user = null;

        //when + then
        assertThatThrownBy(() -> service.getEmployees(OWNER_EMAIL, BRANCH_ID))
                .isInstanceOf(AuthenticatedUserMissingException.class);
    }
}
