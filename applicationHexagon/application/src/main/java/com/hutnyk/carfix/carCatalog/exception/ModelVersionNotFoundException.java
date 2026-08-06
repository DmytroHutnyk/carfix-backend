package com.hutnyk.carfix.carCatalog.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class ModelVersionNotFoundException extends NotFoundException {

    public ModelVersionNotFoundException(Integer modelVersionId) {
        super(CarCatalogErrorCode.MODEL_VERSION_NOT_FOUND, "Model version", modelVersionId);
    }
}
