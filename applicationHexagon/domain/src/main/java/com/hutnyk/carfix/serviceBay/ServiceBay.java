package com.hutnyk.carfix.serviceBay;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ServiceBay {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;
    private final ServiceBayStatus status;

    //Nullable
    private final String notes;
    private final Integer serviceBayTypeId;
    private final BranchId branchId;

    @Builder
    private ServiceBay(
            Integer id,
            String name,
            ServiceBayStatus status,
            String notes,
            Integer serviceBayTypeId,
            BranchId branchId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.status = Validator.notNull(status, "status");
        this.notes = notes;
        this.serviceBayTypeId = Validator.notNull(serviceBayTypeId, "serviceBayTypeId");
        this.branchId = Validator.notNull(branchId, "branchId");
    }

    public static ServiceBay of(
            Integer id,
            String name,
            ServiceBayStatus status,
            String notes,
            Integer serviceBayTypeId,
            BranchId branchId) {
        return ServiceBay.builder()
                .id(id)
                .name(name)
                .status(status)
                .notes(notes)
                .serviceBayTypeId(serviceBayTypeId)
                .branchId(branchId)
                .build();
    }

    /** A bay the owner registers: active from day one, no notes yet. */
    public static ServiceBay create(String name, Integer serviceBayTypeId, BranchId branchId) {
        return of(null, name, ServiceBayStatus.ACTIVE, null, serviceBayTypeId, branchId);
    }
}
