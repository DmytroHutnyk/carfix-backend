package com.hutnyk.carfix.carCatalog;

public class CarCatalogMapper {

    public static CarBrand toDomain(CarBrandEntity e) {
        if (e == null) return null;
        return CarBrand.of(e.getId(), e.getName());
    }

    public static CarModel toDomain(CarModelEntity e) {
        if (e == null) return null;
        return CarModel.of(e.getId(), e.getName(), e.getCarBrandEntity().getId());
    }

    public static ModelGeneration toDomain(ModelGenerationEntity e) {
        if (e == null) return null;
        return ModelGeneration.of(
                e.getId(),
                e.getName(),
                e.getStartProduction(),
                e.getEndProduction(),
                e.getCarModelEntity().getId()
        );
    }
}
