package com.hutnyk.carfix.user.mapper;

import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.address.query.LocationView;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.dto.response.AddressResponse;
import com.hutnyk.carfix.user.dto.response.LocationResponse;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;

public class UserToResponseMapper {
    public static UserCoreResponse toCoreResponse(User user, AddressView address, LocationView preferredLocation) {
        if (user == null) {
            return null;
        }

        return new UserCoreResponse(
                user.getId().id().toString(),
                user.getName(),
                user.getSurname(),
                user.getPhoneNumber().countryCode(),
                user.getPhoneNumber().phoneNumber(),
                user.getEmail(),
                user.getDateOfBirth(),
                toAddressResponse(address),
                toLocationResponse(preferredLocation)
        );
    }

    public static AddressResponse toAddressResponse(AddressView view) {
        if (view == null) {
            return null;
        }

        return new AddressResponse(
                view.streetName(),
                view.buildingNumber(),
                view.flatNumber(),
                view.postalCode(),
                view.city(),
                view.region(),
                view.countryIso(),
                view.countryName(),
                view.latitude(),
                view.longitude(),
                view.googlePlaceId()
        );
    }

    public static LocationResponse toLocationResponse(LocationView view) {
        if (view == null) {
            return null;
        }

        return new LocationResponse(
                view.city(),
                view.region(),
                view.countryIso(),
                view.latitude(),
                view.longitude()
        );
    }
}
