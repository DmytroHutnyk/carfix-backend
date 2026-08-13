package com.hutnyk.carfix.scheduling.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DaySlotsResponse(LocalDate date, List<SlotResponse> slots) {
}
