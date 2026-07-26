package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.CarCatalogPortOut;
import com.hutnyk.carfix.query.CarBrandView;
import com.hutnyk.carfix.query.CarModelView;
import com.hutnyk.carfix.query.ModelGenerationView;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@PersistenceAdapter
public class CarCatalogAdapterOut implements CarCatalogPortOut {

    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;
    private final ModelGenerationRepository modelGenerationRepository;

    @Override
    public List<CarBrandView> findAllBrands() {
        return carBrandRepository.findAll().stream()
                .map(CarProfileMapper::toBrandView)
                .toList();
    }

    @Override
    public List<CarModelView> findModelsByBrandId(Integer brandId) {
        return carModelRepository.findByCarBrandEntityId(brandId).stream()
                .map(CarProfileMapper::toModelView)
                .toList();
    }

    @Override
    public List<ModelGenerationView> findGenerationsByModelId(Integer modelId) {
        return modelGenerationRepository.findByCarModelEntityId(modelId).stream()
                .map(CarProfileMapper::toGenerationView)
                .toList();
    }

    @Override
    public boolean existsGenerationById(Integer id) {
        return modelGenerationRepository.existsById(id);
    }
}
