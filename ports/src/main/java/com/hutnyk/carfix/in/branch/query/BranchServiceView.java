package com.hutnyk.carfix.in.branch.query;

import java.math.BigDecimal;

public record BranchServiceView(
        Integer serviceId,
        String name,
        //Nullable
        String description,
        Short durationMinutes,
        BigDecimal price
) {}
