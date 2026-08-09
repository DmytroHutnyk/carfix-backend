package com.hutnyk.carfix.in.search.query;

import java.util.List;

public record WorkshopSearchPage(
        List<WorkshopResultView> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        //Nullable until the service attaches it
        SearchEchoView echo
) {
    public static WorkshopSearchPage empty(int page, int size) {
        return new WorkshopSearchPage(List.of(), page, size, 0, 0, null);
    }

    public WorkshopSearchPage withEcho(SearchEchoView echo) {
        return new WorkshopSearchPage(content, page, size, totalElements, totalPages, echo);
    }
}
