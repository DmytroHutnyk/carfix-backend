package com.hutnyk.carfix.in.search.query;

import java.util.List;

public record WorkshopSearchPage(
        List<WorkshopResultView> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static WorkshopSearchPage empty(int page, int size) {
        return new WorkshopSearchPage(List.of(), page, size, 0, 0);
    }
}
