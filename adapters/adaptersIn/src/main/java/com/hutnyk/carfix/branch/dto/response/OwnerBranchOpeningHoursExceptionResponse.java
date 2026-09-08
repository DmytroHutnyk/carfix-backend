package com.hutnyk.carfix.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record OwnerBranchOpeningHoursExceptionResponse(
        Integer id,
        LocalDate date,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime opensAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime closesAt,
        boolean isOpen,
        String reason
) {}
