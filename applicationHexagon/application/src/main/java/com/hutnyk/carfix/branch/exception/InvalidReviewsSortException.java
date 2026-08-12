package com.hutnyk.carfix.branch.exception;

import com.hutnyk.carfix.exception.ValidationException;

public class InvalidReviewsSortException extends ValidationException {

    public InvalidReviewsSortException(String sort) {
        super(BranchErrorCode.INVALID_REVIEWS_SORT, "Invalid reviews sort: " + sort, null, null);
    }
}
