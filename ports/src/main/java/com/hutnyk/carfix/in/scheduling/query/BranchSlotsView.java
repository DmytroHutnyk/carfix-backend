package com.hutnyk.carfix.in.scheduling.query;

import java.util.List;

public record BranchSlotsView(String tz, boolean chainable, List<DaySlotsView> days) {}
