package com.hutnyk.carfix.branch.exception;

import com.hutnyk.carfix.exception.NotFoundException;

import java.util.UUID;

/** Missing and inactive branches share one response to avoid revealing valid ids. */
public class BranchNotFoundException extends NotFoundException {

    public BranchNotFoundException(UUID branchId) {
        super(BranchErrorCode.BRANCH_NOT_FOUND, "Branch", branchId);
    }
}
