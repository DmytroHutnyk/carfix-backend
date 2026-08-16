package com.hutnyk.carfix.in.scheduling.query;

import java.time.LocalDate;
import java.util.List;

public record DaySlotsView(LocalDate date, List<SlotView> slots) {}
