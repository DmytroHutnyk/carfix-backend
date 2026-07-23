package com.hutnyk.carfix.adapter;

import com.hutnyk.carfix.carProfile.CarBrand;
import com.hutnyk.carfix.carProfile.CarModel;
import com.hutnyk.carfix.carProfile.ModelGeneration;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.mapper.CarProfileMapper;
import com.hutnyk.carfix.out.CarCatalogPortOut;
import com.hutnyk.carfix.repository.CarBrandRepository;
import com.hutnyk.carfix.repository.CarModelRepository;
import com.hutnyk.carfix.repository.ModelGenerationRepository;
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
                .map(CarProfileMapper::toBrandDomain)
                .toList();
    }

    @Override
    public List<CarModel> findModelsByBrandId(Integer brandId) {
        return carModelRepository.findByCarBrandEntityId(brandId).stream()
                .map(CarProfileMapper::toModelDomain)
                .toList();
    }

    @Override
    public List<ModelGeneration> findGenerationsByModelId(Integer modelId) {
        return modelGenerationRepository.findByCarModelEntityId(modelId).stream()
                .map(CarProfileMapper::toGenerationDomain)
                .toList();
    }

    @Override
    public boolean existsGenerationById(Integer id) {
        return modelGenerationRepository.existsById(id);
    }
}
