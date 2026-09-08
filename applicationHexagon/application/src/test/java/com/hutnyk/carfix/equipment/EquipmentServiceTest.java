package com.hutnyk.carfix.equipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.equipment.exception.EquipmentNotFoundException;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.in.branch.query.OwnerBranchDetailView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.in.equipment.commands.CreateEquipmentCommand;
import com.hutnyk.carfix.in.equipment.commands.UpdateEquipmentCommand;
import com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.equipment.EquipmentPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EquipmentServiceTest {

    private static final String EMAIL = "owner@carfix.dev";
    private static final UUID BRANCH_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");

    private static User ownerUser() {
        return User.builder()
                .id(UserId.genId())
                .name("Marek")
                .surname("Kowalski")
                .phoneNumber(new PhoneNumber("+48", "600100200"))
                .email(EMAIL)
                .role(UserRole.OWNER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .build();
    }

    private final FakeUserPortOut users = new FakeUserPortOut();
    private final FakeOwnerBranchPortOut ownerBranch = new FakeOwnerBranchPortOut();
    private final FakeEquipmentPortOut equipmentPort = new FakeEquipmentPortOut();
    private final EquipmentService service = new EquipmentService(equipmentPort, ownerBranch, users);

    @BeforeEach
    public void ownerOwnsTheBranch() {
        User user = ownerUser();
        users.user = user;
        ownerBranch.ownedBranchId = BRANCH_ID;
        ownerBranch.expectedOwnerId = user.getId();
    }

    @Test
    public void test_create_reuses_an_existing_type_case_insensitively() {
        //given
        equipmentPort.types.add(EquipmentType.of(7, "Lift", null));
        int typesBefore = equipmentPort.types.size();

        //when
        OwnerEquipmentView view = service.createEquipment(EMAIL, BRANCH_ID,
                new CreateEquipmentCommand("2-post lift #1", "lift", "  "));

        //then
        assertThat(equipmentPort.types).hasSize(typesBefore);
        assertThat(view.id()).isNotNull();
        assertThat(view.name()).isEqualTo("2-post lift #1");
        assertThat(view.typeName()).isEqualTo("Lift");
        assertThat(view.notes()).isNull();
        assertThat(view.status()).isEqualTo(EquipmentStatus.ACTIVE);
    }

    @Test
    public void test_create_mints_a_branch_type_when_none_matches() {
        //when
        OwnerEquipmentView view = service.createEquipment(EMAIL, BRANCH_ID,
                new CreateEquipmentCommand("Diagnostic scanner", "OBD scanner", "handheld"));

        //then
        assertThat(equipmentPort.types).singleElement().satisfies(type -> {
            assertThat(type.getName()).isEqualTo("OBD scanner");
            assertThat(type.getBranchId()).isEqualTo(BranchId.of(BRANCH_ID));
        });
        assertThat(view.typeName()).isEqualTo("OBD scanner");
        assertThat(view.notes()).isEqualTo("handheld");
    }

    @Test
    public void test_update_preserves_status_and_rewrites_fields() {
        //given
        equipmentPort.types.add(EquipmentType.of(7, "Lift", null));
        equipmentPort.equipment.add(
                Equipment.of(42, "old", "worn", EquipmentStatus.SUSPENDED, 7, BranchId.of(BRANCH_ID)));

        //when
        OwnerEquipmentView view = service.updateEquipment(EMAIL, BRANCH_ID, 42,
                new UpdateEquipmentCommand("new lift", "Lift", "serviced"));

        //then
        assertThat(view.id()).isEqualTo(42);
        assertThat(view.status()).isEqualTo(EquipmentStatus.SUSPENDED);
        assertThat(view.name()).isEqualTo("new lift");
        assertThat(view.typeName()).isEqualTo("Lift");
        assertThat(view.notes()).isEqualTo("serviced");
    }

    @Test
    public void test_branch_not_owned_is_branch_not_found() {
        //given
        ownerBranch.expectedOwnerId = UserId.genId();

        //when + then
        assertThatThrownBy(() -> service.getEquipment(EMAIL, BRANCH_ID))
                .isInstanceOf(BranchNotFoundException.class);
    }

    @Test
    public void test_unknown_equipment_is_equipment_not_found() {
        //when + then
        assertThatThrownBy(() -> service.updateEquipment(EMAIL, BRANCH_ID, 99,
                new UpdateEquipmentCommand("x", "Lift", null)))
                .isInstanceOf(EquipmentNotFoundException.class);
    }

    @Test
    public void test_blank_type_is_a_domain_validation_error() {
        //when + then
        assertThatThrownBy(() -> service.createEquipment(EMAIL, BRANCH_ID,
                new CreateEquipmentCommand("Some tool", "   ", "notes")))
                .isInstanceOf(DomainObjectValidationException.class);
    }

    private static final class FakeUserPortOut implements UserPortOut {
        User user;

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.ofNullable(user);
        }

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }

        @Override
        public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
            return false;
        }

        @Override
        public User update(User u) {
            return u;
        }
    }

    private static final class FakeOwnerBranchPortOut implements OwnerBranchPortOut {
        UUID ownedBranchId;
        UserId expectedOwnerId;

        @Override
        public List<OwnerBranchSummaryView> findSummariesByOwnerId(UserId ownerId, Instant now) {
            return List.of();
        }

        @Override
        public Optional<OwnerBranchDetailView> findDetailByIdAndOwnerId(UUID branchId, UserId ownerId) {
            if (branchId.equals(ownedBranchId) && ownerId.equals(expectedOwnerId)) {
                return Optional.of(new OwnerBranchDetailView(
                        branchId, null, null, null, null, null, null, null, null, null, null, null));
            }
            return Optional.empty();
        }
    }

    private static final class FakeEquipmentPortOut implements EquipmentPortOut {
        final List<Equipment> equipment = new ArrayList<>();
        final List<EquipmentType> types = new ArrayList<>();
        int nextId = 1;
        int nextTypeId = 500;

        @Override
        public EquipmentType insertType(EquipmentType type) {
            EquipmentType stored = EquipmentType.of(nextTypeId++, type.getName(), type.getBranchId());
            types.add(stored);
            return stored;
        }

        @Override
        public Equipment insert(Equipment e) {
            Equipment stored = Equipment.of(
                    nextId++, e.getName(), e.getNotes(), e.getStatus(), e.getEquipmentTypeId(), e.getBranchId());
            equipment.add(stored);
            return stored;
        }

        @Override
        public Equipment update(Equipment e) {
            equipment.removeIf(x -> x.getId().equals(e.getId()));
            equipment.add(e);
            return e;
        }

        @Override
        public List<OwnerEquipmentView> findViewsByBranchId(UUID branchId) {
            return equipment.stream()
                    .filter(x -> x.getBranchId().id().equals(branchId))
                    .map(x -> new OwnerEquipmentView(
                            x.getId(), x.getName(), typeName(x.getEquipmentTypeId()), x.getNotes(), x.getStatus()))
                    .toList();
        }

        @Override
        public Optional<Equipment> findByIdAndBranchId(Integer equipmentId, UUID branchId) {
            return equipment.stream()
                    .filter(x -> x.getId().equals(equipmentId) && x.getBranchId().id().equals(branchId))
                    .findFirst();
        }

        @Override
        public Optional<EquipmentType> findTypeByNameForBranch(String name, UUID branchId) {
            return types.stream()
                    .filter(t -> t.getName().equalsIgnoreCase(name)
                            && (t.getBranchId() == null || t.getBranchId().id().equals(branchId)))
                    .findFirst();
        }

        private String typeName(Integer typeId) {
            return types.stream()
                    .filter(t -> t.getId().equals(typeId))
                    .map(EquipmentType::getName)
                    .findFirst()
                    .orElse(null);
        }
    }
}
