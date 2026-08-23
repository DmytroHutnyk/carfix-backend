package com.hutnyk.carfix.in.branch.query;

import java.time.LocalDate;
import java.time.LocalTime;

public record OwnerBranchOpeningHoursExceptionView(
        Integer id,
        LocalDate date,
        //Nullable pair — null on a closed day
        LocalTime opensAt,
        LocalTime closesAt,
        boolean isOpen,
        String reason
) {}
