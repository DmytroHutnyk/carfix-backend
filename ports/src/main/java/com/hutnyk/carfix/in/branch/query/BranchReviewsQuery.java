package com.hutnyk.carfix.in.branch.query;

import java.util.UUID;

public record BranchReviewsQuery(
        UUID branchId,
        //Nullable on the way in; the service normalizes to one of the SORT_* constants
        String sort,
        int page,
        int size
) {
    public static final String SORT_NEWEST = "newest";
    public static final String SORT_HIGHEST = "highest";
    public static final String SORT_LOWEST = "lowest";
}
