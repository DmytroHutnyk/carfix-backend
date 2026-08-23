package com.hutnyk.carfix.in.branch.commands;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateBranchOpeningHoursExceptionCommand(
        LocalDate date,
        LocalTime opensAt,
        LocalTime closesAt,
        boolean isOpen,
        String reason
) {
}
