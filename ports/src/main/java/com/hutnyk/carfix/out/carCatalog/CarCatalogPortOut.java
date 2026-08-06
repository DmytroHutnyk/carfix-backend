package com.hutnyk.carfix.out.carCatalog;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelVersion;

import java.util.List;

public interface CarCatalogPortOut {
    List<CarBrand> findAllBrands();
    List<CarModel> findModelsByBrandId(Integer brandId);
    List<ModelVersion> findVersionsByModelId(Integer modelId);
    boolean existsVersionById(Integer id);
}
