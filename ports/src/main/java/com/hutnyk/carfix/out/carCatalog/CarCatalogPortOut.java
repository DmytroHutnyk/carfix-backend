package com.hutnyk.carfix.out.carCatalog;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelGeneration;

import java.util.List;

public interface CarCatalogPortOut {
    List<CarBrand> findAllBrands();
    List<CarModel> findModelsByBrandId(Integer brandId);
    List<ModelGeneration> findGenerationsByModelId(Integer modelId);
    boolean existsGenerationById(Integer id);
}
