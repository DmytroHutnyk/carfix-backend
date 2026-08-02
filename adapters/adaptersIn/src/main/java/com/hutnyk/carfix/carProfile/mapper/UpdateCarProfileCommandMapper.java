package com.hutnyk.carfix.carProfile.mapper;

import com.hutnyk.carfix.carProfile.dto.request.UpdateCarProfileRequest;
import com.hutnyk.carfix.in.carProfile.commands.UpdateCarProfileCommand;

public class UpdateCarProfileCommandMapper {
    public static UpdateCarProfileCommand toCommand(UpdateCarProfileRequest request) {
        if (request == null) return null;
        return new UpdateCarProfileCommand(
                request.name(),
                request.modelVersionId(),
                request.vin(),
                request.plates(),
                request.insuranceDate(),
                request.serviceCertificateDate()
        );
    }
}
