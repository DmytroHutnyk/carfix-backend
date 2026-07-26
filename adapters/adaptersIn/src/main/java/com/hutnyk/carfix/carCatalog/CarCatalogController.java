package com.hutnyk.carfix.carCatalog;

import com.hutnyk.carfix.carCatalog.dto.response.CarBrandResponse;
import com.hutnyk.carfix.carCatalog.dto.response.CarModelResponse;
import com.hutnyk.carfix.carCatalog.dto.response.ModelGenerationResponse;
import com.hutnyk.carfix.in.CarCatalogPortIn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/car-catalog")
public class CarCatalogController {

    private final CarCatalogPortIn carCatalogPortIn;

    @GetMapping("/brands")
    public ResponseEntity<List<CarBrandResponse>> getBrands() {
        List<CarBrandResponse> response = carCatalogPortIn.getAllBrands().stream()
                .map(CarCatalogResponseMapper::toBrandResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brands/{brandId}/models")
    public ResponseEntity<List<CarModelResponse>> getModels(@PathVariable Integer brandId) {
        List<CarModelResponse> response = carCatalogPortIn.getModelsByBrandId(brandId).stream()
                .map(CarCatalogResponseMapper::toModelResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/models/{modelId}/generations")
    public ResponseEntity<List<ModelGenerationResponse>> getGenerations(@PathVariable Integer modelId) {
        List<ModelGenerationResponse> response = carCatalogPortIn.getGenerationsByModelId(modelId).stream()
                .map(CarCatalogResponseMapper::toGenerationResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
