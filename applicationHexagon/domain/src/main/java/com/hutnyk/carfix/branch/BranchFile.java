package com.hutnyk.carfix.branch;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class BranchFile {

    @EqualsAndHashCode.Include
    private final BranchId branchId;

    @EqualsAndHashCode.Include
    private final Integer fileId;
    private final Boolean isPublic;
    private final Integer displayOrder;

    @Builder
    private BranchFile(BranchId branchId, Integer fileId, Boolean isPublic, Integer displayOrder) {
        this.branchId = Validator.notNull(branchId, "branchId");
        this.fileId = Validator.notNull(fileId, "fileId");
        this.isPublic = Validator.notNull(isPublic, "isPublic");
        this.displayOrder = Validator.notNull(displayOrder, "displayOrder");
    }

    public static BranchFile of(BranchId branchId, Integer fileId, Boolean isPublic, Integer displayOrder) {
        return BranchFile.builder()
                .branchId(branchId)
                .fileId(fileId)
                .isPublic(isPublic)
                .displayOrder(displayOrder)
                .build();
    }
}
