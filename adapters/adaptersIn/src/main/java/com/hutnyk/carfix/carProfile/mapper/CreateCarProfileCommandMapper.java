package com.hutnyk.carfix.carProfile.mapper;

import com.hutnyk.carfix.carProfile.dto.request.CreateCarProfileRequest;
import com.hutnyk.carfix.in.carProfile.commands.CreateCarProfileCommand;

public class CreateCarProfileCommandMapper {
    public static CreateCarProfileCommand toCommand(CreateCarProfileRequest request) {
        if (request == null) return null;
        return new CreateCarProfileCommand(
                request.name(),
                request.vin(),
                request.plates(),
                request.serviceCertificateDate(),
                request.insuranceDate(),
                request.modelGenerationId()
        );
    }
}
