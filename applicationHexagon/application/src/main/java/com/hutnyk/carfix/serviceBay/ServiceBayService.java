package com.hutnyk.carfix.serviceBay;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.serviceBay.OwnerServiceBayPortIn;
import com.hutnyk.carfix.in.serviceBay.commands.CreateServiceBayCommand;
import com.hutnyk.carfix.in.serviceBay.commands.UpdateServiceBayCommand;
import com.hutnyk.carfix.in.serviceBay.query.OwnerServiceBayView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.serviceBay.ServiceBayPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.serviceBay.exception.ServiceBayNotFoundException;
import com.hutnyk.carfix.serviceBay.exception.ServiceBayTypeNotFoundException;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@ApplicationService
public class ServiceBayService implements OwnerServiceBayPortIn {

    private final ServiceBayPortOut serviceBayPortOut;
    private final OwnerBranchPortOut ownerBranchPortOut;
    private final UserPortOut userPortOut;

    @Override
    @Transactional(readOnly = true)
    public List<OwnerServiceBayView> getServiceBays(String ownerEmail, UUID branchId) {
        requireOwnedBranch(ownerEmail, branchId);
        return serviceBayPortOut.findViewsByBranchId(branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceBayTypeView> getServiceBayTypes(String ownerEmail, UUID branchId) {
        requireOwnedBranch(ownerEmail, branchId);
        return serviceBayPortOut.findTypesForBranch(branchId);
    }

    @Override
    public OwnerServiceBayView createServiceBay(String ownerEmail, UUID branchId, CreateServiceBayCommand command) {
        requireOwnedBranch(ownerEmail, branchId);
        requireTypeForBranch(command.serviceBayTypeId(), branchId);
        ServiceBay created = serviceBayPortOut.insert(ServiceBay.create(
                command.name().trim(), command.serviceBayTypeId(), normalizeNotes(command.notes()), BranchId.of(branchId)));
        return serviceBayPortOut.findViewByIdAndBranchId(created.getId(), branchId)
                .orElseThrow(() -> new ServiceBayNotFoundException(created.getId()));
    }

    @Override
    public OwnerServiceBayView updateServiceBay(
            String ownerEmail, UUID branchId, Integer bayId, UpdateServiceBayCommand command) {
        requireOwnedBranch(ownerEmail, branchId);
        ServiceBay existing = serviceBayPortOut.findByIdAndBranchId(bayId, branchId)
                .orElseThrow(() -> new ServiceBayNotFoundException(bayId));
        requireTypeForBranch(command.serviceBayTypeId(), branchId);
        serviceBayPortOut.update(existing.update(
                command.name().trim(), command.serviceBayTypeId(), normalizeNotes(command.notes())));
        return serviceBayPortOut.findViewByIdAndBranchId(bayId, branchId)
                .orElseThrow(() -> new ServiceBayNotFoundException(bayId));
    }

    private void requireOwnedBranch(String ownerEmail, UUID branchId) {
        User owner = userPortOut.loadUserByEmail(ownerEmail)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(ownerEmail));
        ownerBranchPortOut.findDetailByIdAndOwnerId(branchId, owner.getId())
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    private void requireTypeForBranch(Integer serviceBayTypeId, UUID branchId) {
        if (!serviceBayPortOut.existsTypeForBranch(serviceBayTypeId, branchId)) {
            throw new ServiceBayTypeNotFoundException(serviceBayTypeId);
        }
    }

    private static String normalizeNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        return notes.trim();
    }
}
