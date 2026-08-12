package com.hutnyk.carfix.branch.exception;

import com.hutnyk.carfix.exception.NotFoundException;

import java.util.UUID;

/**
 * No ACTIVE branch with this id. Also thrown for existing but non-ACTIVE branches —
 * a distinct status would confirm the id is real.
 */
public class BranchNotFoundException extends NotFoundException {

    public BranchNotFoundException(UUID branchId) {
        super(BranchErrorCode.BRANCH_NOT_FOUND, "Branch", branchId);
    }
}
