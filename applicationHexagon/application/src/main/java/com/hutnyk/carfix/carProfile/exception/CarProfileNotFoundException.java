package com.hutnyk.carfix.carProfile.exception;

import com.hutnyk.carfix.exception.NotFoundException;

import java.util.UUID;

/** Missing and foreign profiles share one response to avoid revealing valid ids. */
public class CarProfileNotFoundException extends NotFoundException {

    public CarProfileNotFoundException(UUID profileId) {
        super(CarProfileErrorCode.CAR_PROFILE_NOT_FOUND, "Car profile", profileId);
    }
}
