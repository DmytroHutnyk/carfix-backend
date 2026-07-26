package com.hutnyk.carfix.carCatalog;

import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.CarCatalogPortIn;
import com.hutnyk.carfix.out.CarCatalogPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ApplicationService
@RequiredArgsConstructor
public class CarCatalogService implements CarCatalogPortIn {

    private final CarCatalogPortOut carCatalogPortOut;

    @Override
    @Transactional(readOnly = true)
    public List<CarBrand> getAllBrands() {
        return carCatalogPortOut.findAllBrands();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarModel> getModelsByBrandId(Integer brandId) {
        return carCatalogPortOut.findModelsByBrandId(brandId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModelGeneration> getGenerationsByModelId(Integer modelId) {
        return carCatalogPortOut.findGenerationsByModelId(modelId);
    }
}
