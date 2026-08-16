package com.hutnyk.carfix.in.scheduling.query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BranchSlotsQuery(UUID branchId, List<Integer> serviceIds, LocalDate from, LocalDate to) {}
