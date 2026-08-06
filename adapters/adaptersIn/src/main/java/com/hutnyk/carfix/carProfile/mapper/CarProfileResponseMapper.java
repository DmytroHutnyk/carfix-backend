package com.hutnyk.carfix.carProfile.mapper;

import com.hutnyk.carfix.carProfile.dto.response.CarProfileResponse;
import com.hutnyk.carfix.in.carProfile.query.CarProfileView;


//TODO why view?
public class CarProfileResponseMapper {
    public static CarProfileResponse toResponse(CarProfileView view) {
        if (view == null) return null;
        return new CarProfileResponse(
                view.id().toString(),
                view.name(),
                view.vin(),
                view.plates(),
                view.serviceCertificateDate(),
                view.insuranceDate(),
                view.brandId(),
                view.brandName(),
                view.modelId(),
                view.modelName(),
                view.versionId(),
                view.versionName()
        );
    }
}
