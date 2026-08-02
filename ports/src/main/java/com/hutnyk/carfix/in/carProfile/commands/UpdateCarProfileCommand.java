package com.hutnyk.carfix.in.carProfile.commands;

import java.time.LocalDate;

public record UpdateCarProfileCommand(
        String name,
        Integer modelVersionId,
        String vin,
        String plates,
        LocalDate insuranceDate,
        LocalDate serviceCertificateDate
) {}
