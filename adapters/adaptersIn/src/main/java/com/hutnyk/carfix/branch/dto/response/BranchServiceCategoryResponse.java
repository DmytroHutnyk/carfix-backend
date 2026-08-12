package com.hutnyk.carfix.branch.dto.response;

import java.util.List;

public record BranchServiceCategoryResponse(
        Integer categoryId,
        String name,
        List<BranchServiceResponse> services
) {}
