package com.hutnyk.carfix.carCatalog.adapter;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelVersion;
import com.hutnyk.carfix.carCatalog.mapper.CarCatalogMapper;
import com.hutnyk.carfix.carCatalog.repository.CarBrandRepository;
import com.hutnyk.carfix.carCatalog.repository.CarModelRepository;
import com.hutnyk.carfix.carCatalog.repository.ModelVersionRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.carCatalog.CarCatalogPortOut;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@PersistenceAdapter
public class CarCatalogAdapterOut implements CarCatalogPortOut {

    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;
    private final ModelVersionRepository modelVersionRepository;

    @Override
    public List<CarBrand> findAllBrands() {
        return carBrandRepository.findAll().stream()
                .map(CarCatalogMapper::toDomain)
                .toList();
    }

    @Override
    public List<CarModel> findModelsByBrandId(Integer brandId) {
        return carModelRepository.findByCarBrandEntityId(brandId).stream()
                .map(CarCatalogMapper::toDomain)
                .toList();
    }

    @Override
    public List<ModelVersion> findVersionsByModelId(Integer modelId) {
        return modelVersionRepository.findByCarModelEntityId(modelId).stream()
                .map(CarCatalogMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsVersionById(Integer id) {
        return modelVersionRepository.existsById(id);
    }
}
