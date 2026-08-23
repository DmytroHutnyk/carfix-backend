package com.hutnyk.carfix.carCatalog.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class CarBrandNotFoundException extends NotFoundException {

    public CarBrandNotFoundException(Integer carBrandId) {
        super(CarCatalogErrorCode.CAR_BRAND_NOT_FOUND, "Car brand", carBrandId);
    }
}
