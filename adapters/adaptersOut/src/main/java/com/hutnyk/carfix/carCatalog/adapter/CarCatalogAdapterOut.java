package com.hutnyk.carfix.carCatalog.adapter;

import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.CarModel;
import com.hutnyk.carfix.carCatalog.ModelGeneration;
import com.hutnyk.carfix.carCatalog.mapper.CarCatalogMapper;
import com.hutnyk.carfix.carCatalog.repository.CarBrandRepository;
import com.hutnyk.carfix.carCatalog.repository.CarModelRepository;
import com.hutnyk.carfix.carCatalog.repository.ModelGenerationRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.carCatalog.CarCatalogPortOut;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@PersistenceAdapter
public class CarCatalogAdapterOut implements CarCatalogPortOut {

    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;
    private final ModelGenerationRepository modelGenerationRepository;

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
    public List<ModelGeneration> findGenerationsByModelId(Integer modelId) {
        return modelGenerationRepository.findByCarModelEntityId(modelId).stream()
                .map(CarCatalogMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsGenerationById(Integer id) {
        return modelGenerationRepository.existsById(id);
    }
}
