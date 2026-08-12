package com.hutnyk.carfix.branch.dto.response;

public record BranchOpeningHoursResponse(
        String dayOfWeek,
        String startTime,
        String closeTime
) {}
