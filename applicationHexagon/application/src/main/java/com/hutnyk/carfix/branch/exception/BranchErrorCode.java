package com.hutnyk.carfix.branch.exception;

import com.hutnyk.carfix.exception.ErrorCategory;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BranchErrorCode implements ErrorCode {

    BRANCH_NOT_FOUND(ErrorCategory.NOT_FOUND),
    INVALID_REVIEWS_SORT(ErrorCategory.VALIDATION),
    INVALID_BRANCH_REGISTRATION(ErrorCategory.VALIDATION);

    private final ErrorCategory category;

    @Override
    public String code() {
        return name();
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}
