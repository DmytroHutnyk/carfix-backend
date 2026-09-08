package com.hutnyk.carfix.equipment;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.equipment.exception.EquipmentNotFoundException;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.equipment.OwnerEquipmentPortIn;
import com.hutnyk.carfix.in.equipment.commands.CreateEquipmentCommand;
import com.hutnyk.carfix.in.equipment.commands.UpdateEquipmentCommand;
import com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.equipment.EquipmentPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationService
@RequiredArgsConstructor
public class EquipmentService implements OwnerEquipmentPortIn {

    private final EquipmentPortOut equipmentPortOut;
    private final OwnerBranchPortOut ownerBranchPortOut;
    private final UserPortOut userPortOut;

    @Override
    @Transactional(readOnly = true)
    public List<OwnerEquipmentView> getEquipment(String email, UUID branchId) {
        ensureOwnedBranch(email, branchId);
        return equipmentPortOut.findViewsByBranchId(branchId);
    }

    @Override
    public OwnerEquipmentView createEquipment(String email, UUID branchId, CreateEquipmentCommand command) {
        ensureOwnedBranch(email, branchId);
        Integer typeId = resolveTypeId(command.type(), branchId);
        Equipment created = equipmentPortOut.insert(
                Equipment.create(command.name().trim(), typeId, BranchId.of(branchId), normalizeNotes(command.notes())));
        return reReadView(branchId, created.getId());
    }

    @Override
    public OwnerEquipmentView updateEquipment(
            String email, UUID branchId, Integer equipmentId, UpdateEquipmentCommand command) {
        ensureOwnedBranch(email, branchId);
        Equipment existing = equipmentPortOut.findByIdAndBranchId(equipmentId, branchId)
                .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));
        Integer typeId = resolveTypeId(command.type(), branchId);
        equipmentPortOut.update(existing.update(command.name().trim(), typeId, normalizeNotes(command.notes())));
        return reReadView(branchId, equipmentId);
    }

    private void ensureOwnedBranch(String email, UUID branchId) {
        User user = userPortOut.loadUserByEmail(email)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(email));
        ownerBranchPortOut.findDetailByIdAndOwnerId(branchId, user.getId())
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    private Integer resolveTypeId(String rawType, UUID branchId) {
        String name = rawType == null ? null : rawType.trim();
        return equipmentPortOut.findTypeByNameForBranch(name, branchId)
                .map(EquipmentType::getId)
                .orElseGet(() -> equipmentPortOut.insertType(EquipmentType.create(name, BranchId.of(branchId))).getId());
    }

    private OwnerEquipmentView reReadView(UUID branchId, Integer equipmentId) {
        return equipmentPortOut.findViewsByBranchId(branchId).stream()
                .filter(view -> view.id().equals(equipmentId))
                .findFirst()
                .orElseThrow(() -> new UnexpectedStateException(
                        "Equipment disappeared right after write: " + equipmentId));
    }

    private static String normalizeNotes(String notes) {
        if (notes == null) {
            return null;
        }
        String trimmed = notes.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
