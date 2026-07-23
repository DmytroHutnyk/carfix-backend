package com.hutnyk.carfix.out;

import com.hutnyk.carfix.carProfile.CarBrand;
import com.hutnyk.carfix.carProfile.CarModel;
import com.hutnyk.carfix.carProfile.ModelGeneration;

import java.util.List;

public interface CarCatalogPortOut {
    List<CarBrand> findAllBrands();
    List<CarModel> findModelsByBrandId(Integer brandId);
    List<ModelGeneration> findGenerationsByModelId(Integer modelId);
    boolean existsGenerationById(Integer id);
}
