package com.hutnyk.carfix.serviceBay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.CancellationPolicy;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.in.branch.query.OwnerBranchDetailView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.in.serviceBay.commands.CreateServiceBayCommand;
import com.hutnyk.carfix.in.serviceBay.commands.UpdateServiceBayCommand;
import com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.serviceBay.ServiceBayPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.serviceBay.exception.ServiceBayNotFoundException;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServiceBayServiceTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final UserId OWNER_ID = UserId.genId();
    private static final UUID BRANCH_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");

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

    private static final class StubServiceBayPortOut implements ServiceBayPortOut {
        ServiceBay insertedBay;
        ServiceBay updatedBay;
        Optional<ServiceBay> existing = Optional.empty();
        Optional<OwnerServiceBayView> viewById = Optional.of(new OwnerServiceBayView(100, "Bay", 5, "Basic", null, ServiceBayStatus.ACTIVE));
        List<OwnerServiceBayView> views = List.of();
        List<ServiceBayTypeView> types = List.of();
        final java.util.List<ServiceBayType> typeStore = new java.util.ArrayList<>();
        int nextTypeId = 500;

        @Override public ServiceBayType insertType(ServiceBayType type) {
            ServiceBayType stored = ServiceBayType.of(nextTypeId++, type.getName(), type.getBranchId());
            typeStore.add(stored);
            return stored;
        }
        @Override public ServiceBay insert(ServiceBay bay) {
            this.insertedBay = bay;
            return ServiceBay.of(100, bay.getName(), bay.getStatus(), bay.getNotes(), bay.getServiceBayTypeId(), bay.getBranchId());
        }
        @Override public ServiceBay update(ServiceBay bay) { this.updatedBay = bay; return bay; }
        @Override public List<OwnerServiceBayView> findViewsByBranchId(UUID branchId) { return views; }
        @Override public Optional<OwnerServiceBayView> findViewByIdAndBranchId(Integer bayId, UUID branchId) { return viewById; }
        @Override public Optional<ServiceBay> findByIdAndBranchId(Integer bayId, UUID branchId) { return existing; }
        @Override public List<ServiceBayTypeView> findTypesForBranch(UUID branchId) { return types; }
        @Override public Optional<ServiceBayType> findTypeByNameForBranch(String name, UUID branchId) {
            return typeStore.stream()
                    .filter(t -> t.getName().equalsIgnoreCase(name)
                            && (t.getBranchId() == null || t.getBranchId().id().equals(branchId)))
                    .findFirst();
        }
    }

    private final StubUserPortOut userStub = new StubUserPortOut();
    private final StubOwnerBranchPortOut branchStub = new StubOwnerBranchPortOut();
    private final StubServiceBayPortOut bayStub = new StubServiceBayPortOut();
    private final ServiceBayService service = new ServiceBayService(bayStub, branchStub, userStub);

    @Test
    public void create_builds_active_bay_normalizes_input_and_returns_view() {
        bayStub.typeStore.add(ServiceBayType.of(5, "Basic", null));

        OwnerServiceBayView result = service.createServiceBay(
                EMAIL, BRANCH_ID, new CreateServiceBayCommand("  Bay 1 ", "Basic", "  a note  "));

        assertThat(result).isEqualTo(bayStub.viewById.orElseThrow());
        assertThat(bayStub.insertedBay.getStatus()).isEqualTo(ServiceBayStatus.ACTIVE);
        assertThat(bayStub.insertedBay.getName()).isEqualTo("Bay 1");
        assertThat(bayStub.insertedBay.getNotes()).isEqualTo("a note");
        assertThat(bayStub.insertedBay.getServiceBayTypeId()).isEqualTo(5);
        assertThat(bayStub.insertedBay.getBranchId().id()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void create_blank_notes_is_cleared_to_null() {
        service.createServiceBay(EMAIL, BRANCH_ID, new CreateServiceBayCommand("Bay 1", "Basic", "   "));

        assertThat(bayStub.insertedBay.getNotes()).isNull();
    }

    @Test
    public void create_on_foreign_or_unknown_branch_is_branch_not_found() {
        branchStub.detail = Optional.empty();

        assertThatThrownBy(() -> service.createServiceBay(
                EMAIL, BRANCH_ID, new CreateServiceBayCommand("Bay 1", "Basic", null)))
                .isInstanceOf(BranchNotFoundException.class);
        assertThat(bayStub.insertedBay).isNull();
    }

    @Test
    public void create_reuses_an_existing_type_case_insensitively() {
        bayStub.typeStore.add(ServiceBayType.of(7, "Basic", null));
        int before = bayStub.typeStore.size();

        service.createServiceBay(EMAIL, BRANCH_ID, new CreateServiceBayCommand("Bay 1", "basic", null));

        assertThat(bayStub.typeStore).hasSize(before);
        assertThat(bayStub.insertedBay.getServiceBayTypeId()).isEqualTo(7);
    }

    @Test
    public void create_mints_a_branch_type_when_none_matches() {
        service.createServiceBay(EMAIL, BRANCH_ID, new CreateServiceBayCommand("Bay 1", "With lift", null));

        assertThat(bayStub.typeStore).singleElement().satisfies(type -> {
            assertThat(type.getName()).isEqualTo("With lift");
            assertThat(type.getBranchId()).isEqualTo(com.hutnyk.carfix.branch.BranchId.of(BRANCH_ID));
        });
        assertThat(bayStub.insertedBay.getServiceBayTypeId()).isNotNull();
    }

    @Test
    public void update_preserves_status_replaces_fields_and_returns_view() {
        bayStub.typeStore.add(ServiceBayType.of(7, "With lift", null));
        bayStub.existing = Optional.of(
                ServiceBay.of(42, "Old", ServiceBayStatus.SUSPENDED, "old", 2, com.hutnyk.carfix.branch.BranchId.of(BRANCH_ID)));

        OwnerServiceBayView result = service.updateServiceBay(
                EMAIL, BRANCH_ID, 42, new UpdateServiceBayCommand(" New ", "With lift", " new note "));

        assertThat(result).isEqualTo(bayStub.viewById.orElseThrow());
        assertThat(bayStub.updatedBay.getId()).isEqualTo(42);
        assertThat(bayStub.updatedBay.getStatus()).isEqualTo(ServiceBayStatus.SUSPENDED);
        assertThat(bayStub.updatedBay.getName()).isEqualTo("New");
        assertThat(bayStub.updatedBay.getServiceBayTypeId()).isEqualTo(7);
        assertThat(bayStub.updatedBay.getNotes()).isEqualTo("new note");
    }

    @Test
    public void update_of_unknown_bay_is_service_bay_not_found() {
        bayStub.existing = Optional.empty();

        assertThatThrownBy(() -> service.updateServiceBay(
                EMAIL, BRANCH_ID, 999, new UpdateServiceBayCommand("New", "With lift", null)))
                .isInstanceOf(ServiceBayNotFoundException.class);
        assertThat(bayStub.updatedBay).isNull();
    }

    @Test
    public void update_on_foreign_branch_is_branch_not_found() {
        branchStub.detail = Optional.empty();

        assertThatThrownBy(() -> service.updateServiceBay(
                EMAIL, BRANCH_ID, 42, new UpdateServiceBayCommand("New", "With lift", null)))
                .isInstanceOf(BranchNotFoundException.class);
    }

    @Test
    public void list_returns_views_for_owned_branch() {
        bayStub.views = List.of(new OwnerServiceBayView(1, "Bay 1", 5, "Basic", null, ServiceBayStatus.ACTIVE));

        assertThat(service.getServiceBays(EMAIL, BRANCH_ID)).isEqualTo(bayStub.views);
    }

    @Test
    public void types_returns_types_for_owned_branch() {
        bayStub.types = List.of(new ServiceBayTypeView(5, "Basic"));

        assertThat(service.getServiceBayTypes(EMAIL, BRANCH_ID)).isEqualTo(bayStub.types);
    }
}
