package com.hutnyk.carfix.in;

import com.hutnyk.carfix.carProfile.CarBrand;
import com.hutnyk.carfix.carProfile.CarModel;
import com.hutnyk.carfix.carProfile.ModelGeneration;

import java.util.List;

public interface CarCatalogPortIn {
    List<CarBrand> getAllBrands();
    List<CarModel> getModelsByBrandId(Integer brandId);
    List<ModelGeneration> getGenerationsByModelId(Integer modelId);
}
