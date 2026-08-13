package com.hutnyk.carfix.scheduling.dto.response;

import java.util.List;

public record BranchSlotsResponse(boolean chainable, List<DaySlotsResponse> days) {
}
