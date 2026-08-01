package com.hutnyk.carfix.in.carProfile.commands;

import java.time.LocalDate;

public record CreateCarProfileCommand(
        String name,
        String vin,
        String plates,
        LocalDate serviceCertificateDate,
        LocalDate insuranceDate,
        Integer modelGenerationId
) {}
