package com.hutnyk.carfix.in.carCatalog;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelGeneration;

import java.util.List;

public interface CarCatalogPortIn {
    List<CarBrand> getAllBrands();
    List<CarModel> getModelsByBrandId(Integer brandId);
    List<ModelGeneration> getGenerationsByModelId(Integer modelId);
}
