package com.hutnyk.carfix.branch.dto.response;

import java.math.BigDecimal;

public record BranchServiceResponse(
        Integer serviceId,
        String name,
        String description,
        Short durationMinutes,
        BigDecimal price
) {}
