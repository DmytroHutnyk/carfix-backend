package com.hutnyk.carfix.in.scheduling.query;

import java.time.LocalTime;

public record SlotView(LocalTime startTime, LocalTime endTime) {}
