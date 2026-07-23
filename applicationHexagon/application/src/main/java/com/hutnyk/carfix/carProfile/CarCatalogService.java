package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.CarCatalogPortIn;
import com.hutnyk.carfix.out.CarCatalogPortOut;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationService
@RequiredArgsConstructor
public class CarCatalogService implements CarCatalogPortIn {

    private final CarCatalogPortOut carCatalogPortOut;

    @Override
    public List<CarBrand> getAllBrands() {
        return carCatalogPortOut.findAllBrands();
    }

    @Override
    public List<CarModel> getModelsByBrandId(Integer brandId) {
        return carCatalogPortOut.findModelsByBrandId(brandId);
    }

    @Override
    public List<ModelGeneration> getGenerationsByModelId(Integer modelId) {
        return carCatalogPortOut.findGenerationsByModelId(modelId);
    }
}
