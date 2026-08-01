package com.hutnyk.carfix.carCatalog.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class ModelGenerationNotFoundException extends NotFoundException {

    public ModelGenerationNotFoundException(Integer modelGenerationId) {
        super(CarCatalogErrorCode.MODEL_GENERATION_NOT_FOUND, "Model generation", modelGenerationId);
    }
}
