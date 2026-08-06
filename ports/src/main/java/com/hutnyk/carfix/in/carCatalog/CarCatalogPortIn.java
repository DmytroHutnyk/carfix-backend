package com.hutnyk.carfix.in.carCatalog;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelVersion;

import java.util.List;

public interface CarCatalogPortIn {
    List<CarBrand> getAllBrands();
    List<CarModel> getModelsByBrandId(Integer brandId);
    List<ModelVersion> getVersionsByModelId(Integer modelId);
}
