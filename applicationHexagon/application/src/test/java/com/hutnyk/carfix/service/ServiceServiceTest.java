package com.hutnyk.carfix.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.CancellationPolicy;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.equipment.EquipmentType;
import com.hutnyk.carfix.in.branch.query.OwnerBranchDetailView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.in.service.commands.CreateServiceCommand;
import com.hutnyk.carfix.in.service.commands.ServiceEmployeeRequirementCommand;
import com.hutnyk.carfix.in.service.commands.ServiceEquipmentRequirementCommand;
import com.hutnyk.carfix.in.service.commands.UpdateServiceCommand;
import com.hutnyk.carfix.in.service.query.EmployeeRequirementView;
import com.hutnyk.carfix.in.service.query.EquipmentRequirementView;
import com.hutnyk.carfix.in.service.query.OwnerServiceView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.equipment.EquipmentPortOut;
import com.hutnyk.carfix.out.role.RolePortOut;
import com.hutnyk.carfix.out.service.ServiceCategoryPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.out.serviceBay.ServiceBayPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.role.Role;
import com.hutnyk.carfix.service.exception.ServiceCategoryNotFoundException;
import com.hutnyk.carfix.service.exception.ServiceInUseException;
import com.hutnyk.carfix.service.exception.ServiceNotFoundException;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServiceServiceTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final UserId OWNER_ID = UserId.genId();
    private static final UUID BRANCH_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final int SERVICE_ID = 42;

    private static User ownerUser() {
        return User.builder()
                .id(OWNER_ID)
                .name("Marek")
                .surname("Kowalski")
                .phoneNumber(new PhoneNumber("+48", "600100200"))
                .email(EMAIL)
                .role(UserRole.OWNER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .build();
    }

    private static OwnerBranchDetailView detail() {
        return new OwnerBranchDetailView(
                BRANCH_ID, "AutoFix", BranchStatus.ACTIVE, null, CancellationPolicy.MODERATE,
                null, null, "Europe/Warsaw", null, List.of(), List.of(), List.of());
    }

    private static Service existingService(ServiceStatus status) {
        return Service.of(
                SERVICE_ID, "Old", "old notes", (short) 60, new BigDecimal("10.00"), status,
                BranchId.of(BRANCH_ID), 3, java.util.Set.of(9),
                List.of(EmployeeRequirement.of(1, "old", java.util.Set.of(9))), List.of());
    }

    private static OwnerServiceView view() {
        return new OwnerServiceView(
                100, "Oil Change", "notes", (short) 30, new BigDecimal("49.99"), ServiceStatus.ACTIVE,
                3, "Maintenance", List.of("General"),
                List.of(new EmployeeRequirementView("Mechanic", List.of("Mechanic"))),
                List.of(new EquipmentRequirementView("Oil Drain", List.of("Oil Drain"))));
    }

    private static CreateServiceCommand createCommand() {
        return new CreateServiceCommand(
                "  Oil Change  ", "  notes  ", (short) 30, new BigDecimal("49.99"), 3,
                List.of("General"),
                List.of(new ServiceEmployeeRequirementCommand("Mechanic", List.of("Mechanic"))),
                List.of(new ServiceEquipmentRequirementCommand("Oil Drain", List.of("Oil Drain"))));
    }

    private static UpdateServiceCommand updateCommand() {
        return new UpdateServiceCommand(
                "Full Service", "renamed", (short) 90, new BigDecimal("120.00"), 3,
                List.of("General"),
                List.of(new ServiceEmployeeRequirementCommand("Mechanic", List.of("Mechanic"))),
                List.of());
    }

    private static final class StubUserPortOut implements UserPortOut {
        Optional<User> user = Optional.of(ownerUser());
        @Override public Optional<User> loadUserByEmail(String email) { return user; }
        @Override public boolean existsByEmail(String email) { throw new UnsupportedOperationException(); }
        @Override public boolean existsByPhoneNumber(PhoneNumber phoneNumber) { throw new UnsupportedOperationException(); }
        @Override public User update(User user) { throw new UnsupportedOperationException(); }
    }

    private static final class StubOwnerBranchPortOut implements OwnerBranchPortOut {
        Optional<OwnerBranchDetailView> detail = Optional.of(detail());
        @Override public List<OwnerBranchSummaryView> findSummariesByOwnerId(UserId ownerId, Instant now) { throw new UnsupportedOperationException(); }
        @Override public Optional<OwnerBranchDetailView> findDetailByIdAndOwnerId(UUID branchId, UserId ownerId) { return detail; }
    }

    private static final class StubServiceCategoryPortOut implements ServiceCategoryPortOut {
        List<ServiceCategory> categories = List.of(ServiceCategory.of(3, "Maintenance"));
        @Override public List<ServiceCategory> findAll() { return categories; }
    }

    private static final class StubServiceBayPortOut implements ServiceBayPortOut {
        List<ServiceBayTypeView> types = List.of(new ServiceBayTypeView(5, "General"));
        com.hutnyk.carfix.serviceBay.ServiceBayType insertedType;
        @Override public com.hutnyk.carfix.serviceBay.ServiceBayType insertType(com.hutnyk.carfix.serviceBay.ServiceBayType type) {
            this.insertedType = type;
            return com.hutnyk.carfix.serviceBay.ServiceBayType.of(50, type.getName(), type.getBranchId());
        }
        @Override public com.hutnyk.carfix.serviceBay.ServiceBay insert(com.hutnyk.carfix.serviceBay.ServiceBay bay) { throw new UnsupportedOperationException(); }
        @Override public com.hutnyk.carfix.serviceBay.ServiceBay update(com.hutnyk.carfix.serviceBay.ServiceBay bay) { throw new UnsupportedOperationException(); }
        @Override public List<com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView> findViewsByBranchId(UUID branchId) { throw new UnsupportedOperationException(); }
        @Override public Optional<com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView> findViewByIdAndBranchId(Integer bayId, UUID branchId) { throw new UnsupportedOperationException(); }
        @Override public Optional<com.hutnyk.carfix.serviceBay.ServiceBay> findByIdAndBranchId(Integer bayId, UUID branchId) { throw new UnsupportedOperationException(); }
        @Override public List<ServiceBayTypeView> findTypesForBranch(UUID branchId) { return types; }
        @Override public boolean existsTypeForBranch(Integer typeId, UUID branchId) { throw new UnsupportedOperationException(); }
    }

    private static final class StubRolePortOut implements RolePortOut {
        List<Role> roles = List.of(Role.of(1, "Mechanic", null));
        Role insertedRole;
        @Override public Role insert(Role role) {
            this.insertedRole = role;
            return Role.of(60, role.getName(), role.getBranchId());
        }
        @Override public List<Role> findAllForBranch(UUID branchId) { return roles; }
    }

    private static final class StubEquipmentPortOut implements EquipmentPortOut {
        Optional<EquipmentType> type = Optional.of(EquipmentType.of(7, "Oil Drain", null));
        EquipmentType insertedType;
        @Override public EquipmentType insertType(EquipmentType type) {
            this.insertedType = type;
            return EquipmentType.of(70, type.getName(), type.getBranchId());
        }
        @Override public com.hutnyk.carfix.equipment.Equipment insert(com.hutnyk.carfix.equipment.Equipment equipment) { throw new UnsupportedOperationException(); }
        @Override public com.hutnyk.carfix.equipment.Equipment update(com.hutnyk.carfix.equipment.Equipment equipment) { throw new UnsupportedOperationException(); }
        @Override public List<com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView> findViewsByBranchId(UUID branchId) { throw new UnsupportedOperationException(); }
        @Override public Optional<com.hutnyk.carfix.equipment.Equipment> findByIdAndBranchId(Integer equipmentId, UUID branchId) { throw new UnsupportedOperationException(); }
        @Override public Optional<EquipmentType> findTypeByNameForBranch(String name, UUID branchId) { return type; }
    }

    private static final class StubServicePortOut implements ServicePortOut {
        Service insertedService;
        Service updatedService;
        Service statusUpdatedService;
        Integer deletedId;
        boolean bookingReference;
        Optional<Service> existing = Optional.empty();
        Optional<OwnerServiceView> viewById = Optional.of(view());
        List<OwnerServiceView> views = List.of();

        @Override public List<Service> loadByIds(Collection<Integer> serviceIds) { throw new UnsupportedOperationException(); }

        @Override public Service insert(Service service) {
            this.insertedService = service;
            return Service.of(100, service.getName(), service.getDescription(), service.getDurationMinutes(),
                    service.getPrice(), service.getStatus(), service.getBranchId(), service.getServiceCategoryId(),
                    service.getServiceBayTypeIds(), service.getEmployeeRequirements(), service.getEquipmentRequirements());
        }

        @Override public List<OwnerServiceView> findViewsByBranchId(UUID branchId) { return views; }
        @Override public Optional<OwnerServiceView> findViewByIdAndBranchId(Integer serviceId, UUID branchId) { return viewById; }
        @Override public Optional<Service> findByIdAndBranchId(Integer serviceId, UUID branchId) { return existing; }
        @Override public Service update(Service service) { this.updatedService = service; return service; }
        @Override public Service updateStatus(Service service) { this.statusUpdatedService = service; return service; }
        @Override public void deleteById(Integer serviceId) { this.deletedId = serviceId; }
        @Override public boolean existsBookingReference(Integer serviceId) { return bookingReference; }
    }

    private final StubUserPortOut userStub = new StubUserPortOut();
    private final StubOwnerBranchPortOut branchStub = new StubOwnerBranchPortOut();
    private final StubServiceCategoryPortOut categoryStub = new StubServiceCategoryPortOut();
    private final StubServiceBayPortOut bayStub = new StubServiceBayPortOut();
    private final StubRolePortOut roleStub = new StubRolePortOut();
    private final StubEquipmentPortOut equipmentStub = new StubEquipmentPortOut();
    private final StubServicePortOut serviceStub = new StubServicePortOut();

    private final ServiceService service = new ServiceService(
            serviceStub, branchStub, userStub, categoryStub, bayStub, roleStub, equipmentStub);

    @Test
    public void create_resolves_names_to_ids_builds_active_and_trims_input() {
        OwnerServiceView result = service.createService(EMAIL, BRANCH_ID, createCommand());

        assertThat(result).isEqualTo(view());
        assertThat(serviceStub.insertedService.getId()).isNull();
        assertThat(serviceStub.insertedService.getStatus()).isEqualTo(ServiceStatus.ACTIVE);
        assertThat(serviceStub.insertedService.getName()).isEqualTo("Oil Change");
        assertThat(serviceStub.insertedService.getDescription()).isEqualTo("notes");
        assertThat(serviceStub.insertedService.getServiceCategoryId()).isEqualTo(3);
        assertThat(serviceStub.insertedService.getServiceBayTypeIds()).containsExactly(5);
        assertThat(serviceStub.insertedService.getEmployeeRequirements().getFirst().getRoleIds())
                .containsExactly(1);
        assertThat(serviceStub.insertedService.getEquipmentRequirements().getFirst().getEquipmentTypeIds())
                .containsExactly(7);
    }

    @Test
    public void create_with_unknown_category_is_service_category_not_found() {
        categoryStub.categories = List.of(ServiceCategory.of(99, "Other"));

        assertThatThrownBy(() -> service.createService(EMAIL, BRANCH_ID, createCommand()))
                .isInstanceOf(ServiceCategoryNotFoundException.class);
        assertThat(serviceStub.insertedService).isNull();
    }

    @Test
    public void create_with_unknown_bay_type_name_creates_branch_bay_type() {
        bayStub.types = List.of();

        OwnerServiceView result = service.createService(EMAIL, BRANCH_ID, createCommand());

        assertThat(result).isEqualTo(view());
        assertThat(serviceStub.insertedService.getServiceBayTypeIds()).containsExactly(50);
        assertThat(bayStub.insertedType.getName()).isEqualTo("General");
        assertThat(bayStub.insertedType.getBranchId().id()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void create_reuses_existing_bay_type_case_insensitively() {
        CreateServiceCommand command = new CreateServiceCommand(
                "Oil Change", "notes", (short) 30, new BigDecimal("49.99"), 3,
                List.of("general"),
                List.of(new ServiceEmployeeRequirementCommand("Mechanic", List.of("Mechanic"))),
                List.of(new ServiceEquipmentRequirementCommand("Oil Drain", List.of("Oil Drain"))));

        service.createService(EMAIL, BRANCH_ID, command);

        assertThat(bayStub.insertedType).isNull();
        assertThat(serviceStub.insertedService.getServiceBayTypeIds()).containsExactly(5);
    }

    @Test
    public void create_with_unknown_role_name_creates_branch_role() {
        roleStub.roles = List.of();

        OwnerServiceView result = service.createService(EMAIL, BRANCH_ID, createCommand());

        assertThat(result).isEqualTo(view());
        assertThat(serviceStub.insertedService.getEmployeeRequirements().getFirst().getRoleIds())
                .containsExactly(60);
        assertThat(roleStub.insertedRole.getName()).isEqualTo("Mechanic");
        assertThat(roleStub.insertedRole.getBranchId().id()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void create_with_unknown_equipment_type_name_creates_branch_equipment_type() {
        equipmentStub.type = Optional.empty();

        OwnerServiceView result = service.createService(EMAIL, BRANCH_ID, createCommand());

        assertThat(result).isEqualTo(view());
        assertThat(serviceStub.insertedService.getEquipmentRequirements().getFirst().getEquipmentTypeIds())
                .containsExactly(70);
        assertThat(equipmentStub.insertedType.getName()).isEqualTo("Oil Drain");
        assertThat(equipmentStub.insertedType.getBranchId().id()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void create_on_foreign_or_unknown_branch_is_branch_not_found() {
        branchStub.detail = Optional.empty();

        assertThatThrownBy(() -> service.createService(EMAIL, BRANCH_ID, createCommand()))
                .isInstanceOf(BranchNotFoundException.class);
        assertThat(serviceStub.insertedService).isNull();
    }

    @Test
    public void update_rebuilds_preserving_status_and_replaces_fields() {
        serviceStub.existing = Optional.of(existingService(ServiceStatus.SUSPENDED));

        OwnerServiceView result = service.updateService(EMAIL, BRANCH_ID, SERVICE_ID, updateCommand());

        assertThat(result).isEqualTo(view());
        assertThat(serviceStub.updatedService.getId()).isEqualTo(SERVICE_ID);
        assertThat(serviceStub.updatedService.getStatus()).isEqualTo(ServiceStatus.SUSPENDED);
        assertThat(serviceStub.updatedService.getName()).isEqualTo("Full Service");
        assertThat(serviceStub.updatedService.getDurationMinutes()).isEqualTo((short) 90);
        assertThat(serviceStub.updatedService.getEquipmentRequirements()).isEmpty();
    }

    @Test
    public void update_of_unknown_service_is_service_not_found() {
        serviceStub.existing = Optional.empty();

        assertThatThrownBy(() -> service.updateService(EMAIL, BRANCH_ID, SERVICE_ID, updateCommand()))
                .isInstanceOf(ServiceNotFoundException.class);
        assertThat(serviceStub.updatedService).isNull();
    }

    @Test
    public void update_on_foreign_branch_is_branch_not_found() {
        branchStub.detail = Optional.empty();

        assertThatThrownBy(() -> service.updateService(EMAIL, BRANCH_ID, SERVICE_ID, updateCommand()))
                .isInstanceOf(BranchNotFoundException.class);
    }

    @Test
    public void activate_updates_status_to_active() {
        serviceStub.existing = Optional.of(existingService(ServiceStatus.SUSPENDED));

        service.activateService(EMAIL, BRANCH_ID, SERVICE_ID);

        assertThat(serviceStub.statusUpdatedService.getStatus()).isEqualTo(ServiceStatus.ACTIVE);
    }

    @Test
    public void activate_is_idempotent_when_already_active() {
        serviceStub.existing = Optional.of(existingService(ServiceStatus.ACTIVE));

        service.activateService(EMAIL, BRANCH_ID, SERVICE_ID);

        assertThat(serviceStub.statusUpdatedService.getStatus()).isEqualTo(ServiceStatus.ACTIVE);
    }

    @Test
    public void suspend_updates_status_to_suspended() {
        serviceStub.existing = Optional.of(existingService(ServiceStatus.ACTIVE));

        service.suspendService(EMAIL, BRANCH_ID, SERVICE_ID);

        assertThat(serviceStub.statusUpdatedService.getStatus()).isEqualTo(ServiceStatus.SUSPENDED);
    }

    @Test
    public void activate_of_unknown_service_is_service_not_found() {
        serviceStub.existing = Optional.empty();

        assertThatThrownBy(() -> service.activateService(EMAIL, BRANCH_ID, SERVICE_ID))
                .isInstanceOf(ServiceNotFoundException.class);
        assertThat(serviceStub.statusUpdatedService).isNull();
    }

    @Test
    public void delete_removes_service_when_not_referenced() {
        serviceStub.existing = Optional.of(existingService(ServiceStatus.ACTIVE));
        serviceStub.bookingReference = false;

        service.deleteService(EMAIL, BRANCH_ID, SERVICE_ID);

        assertThat(serviceStub.deletedId).isEqualTo(SERVICE_ID);
    }

    @Test
    public void delete_when_referenced_by_booking_is_service_in_use_and_does_not_delete() {
        serviceStub.existing = Optional.of(existingService(ServiceStatus.ACTIVE));
        serviceStub.bookingReference = true;

        assertThatThrownBy(() -> service.deleteService(EMAIL, BRANCH_ID, SERVICE_ID))
                .isInstanceOf(ServiceInUseException.class);
        assertThat(serviceStub.deletedId).isNull();
    }

    @Test
    public void delete_of_unknown_service_is_service_not_found() {
        serviceStub.existing = Optional.empty();

        assertThatThrownBy(() -> service.deleteService(EMAIL, BRANCH_ID, SERVICE_ID))
                .isInstanceOf(ServiceNotFoundException.class);
        assertThat(serviceStub.deletedId).isNull();
    }

    @Test
    public void list_returns_views_for_owned_branch() {
        serviceStub.views = List.of(view());

        assertThat(service.getServices(EMAIL, BRANCH_ID)).isEqualTo(serviceStub.views);
    }
}
