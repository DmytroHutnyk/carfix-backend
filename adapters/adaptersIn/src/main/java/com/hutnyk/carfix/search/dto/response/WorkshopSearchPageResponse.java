package com.hutnyk.carfix.search.dto.response;

import java.util.List;

public record WorkshopSearchPageResponse(
        List<WorkshopResultResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
