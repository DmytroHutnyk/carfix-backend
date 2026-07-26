package com.hutnyk.carfix.carCatalog;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.CarCatalogPortOut;
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
