package com.hutnyk.carfix.carCatalog.mapper;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelGeneration;
import com.hutnyk.carfix.carCatalog.dto.response.CarBrandResponse;
import com.hutnyk.carfix.carCatalog.dto.response.CarModelResponse;
import com.hutnyk.carfix.carCatalog.dto.response.ModelGenerationResponse;

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
