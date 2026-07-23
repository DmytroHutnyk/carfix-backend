package com.hutnyk.carfix.mapper;

import com.hutnyk.carfix.carProfile.CarBrand;
import com.hutnyk.carfix.carProfile.CarModel;
import com.hutnyk.carfix.carProfile.ModelGeneration;
import com.hutnyk.carfix.dto.response.CarBrandResponse;
import com.hutnyk.carfix.dto.response.CarModelResponse;
import com.hutnyk.carfix.dto.response.ModelGenerationResponse;

public class CarCatalogResponseMapper {
    public static CarBrandResponse toBrandResponse(CarBrand brand) {
        if (brand == null) return null;
        return new CarBrandResponse(brand.getId(), brand.getName());
    }

    public static CarModelResponse toModelResponse(CarModel model) {
        if (model == null) return null;
        return new CarModelResponse(model.getId(), model.getName(), model.getCarBrandId());
    }

    public static ModelGenerationResponse toGenerationResponse(ModelGeneration generation) {
        if (generation == null) return null;
        return new ModelGenerationResponse(
                generation.getId(),
                generation.getName(),
                generation.getStartProduction(),
                generation.getEndProduction(),
                generation.getCarModelId()
        );
    }
}
