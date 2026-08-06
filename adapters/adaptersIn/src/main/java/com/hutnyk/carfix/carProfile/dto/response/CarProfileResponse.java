package com.hutnyk.carfix.carProfile.dto.response;

import java.time.LocalDate;

public record CarProfileResponse(
        String id,
        String name,
        String vin,
        String plates,
        LocalDate serviceCertificateDate,
        LocalDate insuranceDate,
        Integer brandId,
        String brandName,
        Integer modelId,
        String modelName,
        Integer versionId,
        String versionName
) {}
