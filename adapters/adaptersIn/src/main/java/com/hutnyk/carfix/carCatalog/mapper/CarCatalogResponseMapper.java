package com.hutnyk.carfix.carCatalog.mapper;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelVersion;
import com.hutnyk.carfix.carCatalog.dto.response.CarBrandResponse;
import com.hutnyk.carfix.carCatalog.dto.response.CarModelResponse;
import com.hutnyk.carfix.carCatalog.dto.response.ModelVersionResponse;

public class CarCatalogResponseMapper {
    public static CarBrandResponse toBrandResponse(CarBrand brand) {
        if (brand == null) return null;
        return new CarBrandResponse(brand.getId(), brand.getName());
    }

    public static CarModelResponse toModelResponse(CarModel model) {
        if (model == null) return null;
        return new CarModelResponse(model.getId(), model.getName(), model.getCarBrandId());
    }

    public static ModelVersionResponse toVersionResponse(ModelVersion version) {
        if (version == null) return null;
        return new ModelVersionResponse(
                version.getId(),
                version.getName(),
                version.getStartProduction(),
                version.getEndProduction(),
                version.getCarModelId()
        );
    }
}
