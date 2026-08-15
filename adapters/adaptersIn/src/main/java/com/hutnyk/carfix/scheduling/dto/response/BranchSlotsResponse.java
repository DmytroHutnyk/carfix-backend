package com.hutnyk.carfix.scheduling.dto.response;

import java.util.List;

public record BranchSlotsResponse(String tz, boolean chainable, List<DaySlotsResponse> days) {
}
