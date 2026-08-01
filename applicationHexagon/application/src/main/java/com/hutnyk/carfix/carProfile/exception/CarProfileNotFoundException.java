package com.hutnyk.carfix.carProfile.exception;

import com.hutnyk.carfix.exception.NotFoundException;

import java.util.UUID;

/**
 * No car profile with this id belongs to the calling customer.
 * <p>
 * Deliberately also thrown when the profile exists but belongs to somebody else, answering 403
 * there would confirm the id is real.
 */
public class CarProfileNotFoundException extends NotFoundException {

    public CarProfileNotFoundException(UUID profileId) {
        super(CarProfileErrorCode.CAR_PROFILE_NOT_FOUND, "Car profile", profileId);
    }
}
