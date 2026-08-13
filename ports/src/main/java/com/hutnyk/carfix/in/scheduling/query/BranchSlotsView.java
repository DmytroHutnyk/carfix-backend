package com.hutnyk.carfix.in.scheduling.query;

import java.util.List;

public record BranchSlotsView(boolean chainable, List<DaySlotsView> days) {}
