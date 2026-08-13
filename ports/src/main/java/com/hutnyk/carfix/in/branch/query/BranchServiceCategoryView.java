package com.hutnyk.carfix.in.branch.query;

import java.util.List;

public record BranchServiceCategoryView(
        Integer categoryId,
        String name,
        List<BranchServiceView> services
) {}
