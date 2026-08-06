package com.hutnyk.carfix.in.carProfile.query;

import java.time.LocalDate;
import java.util.UUID;

public record CarProfileView(
        UUID id,
        String name,

        //Nullable
        String vin,

        //Nullable
        String plates,

        //Nullable
        LocalDate serviceCertificateDate,

        //Nullable
        LocalDate insuranceDate,
        UUID customerId,

        //Nullable
        Integer fileId,
        Integer brandId,
        String brandName,
        Integer modelId,
        String modelName,
        Integer versionId,
        String versionName
) {}
