package com.hutnyk.carfix.in.branch.query;

import java.util.List;

public record BranchReviewsPage(
        List<BranchReviewView> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static BranchReviewsPage empty(int page, int size) {
        return new BranchReviewsPage(List.of(), page, size, 0, 0);
    }
}
