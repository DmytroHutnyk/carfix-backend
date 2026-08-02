package com.hutnyk.carfix.carCatalog.mapper;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelVersion;
import com.hutnyk.carfix.carCatalog.entity.CarBrandEntity;
import com.hutnyk.carfix.carCatalog.entity.CarModelEntity;
import com.hutnyk.carfix.carCatalog.entity.ModelVersionEntity;

public class CarCatalogMapper {

    public static CarBrand toDomain(CarBrandEntity e) {
        if (e == null) return null;
        return CarBrand.of(e.getId(), e.getName());
    }

    public static CarModel toDomain(CarModelEntity e) {
        if (e == null) return null;
        return CarModel.of(e.getId(), e.getName(), e.getCarBrandEntity().getId());
    }

    public static ModelVersion toDomain(ModelVersionEntity e) {
        if (e == null) return null;
        return ModelVersion.of(
                e.getId(),
                e.getName(),
                e.getStartProduction(),
                e.getEndProduction(),
                e.getCarModelEntity().getId()
        );
    }
}
