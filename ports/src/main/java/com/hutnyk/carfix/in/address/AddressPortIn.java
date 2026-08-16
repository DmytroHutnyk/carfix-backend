package com.hutnyk.carfix.in.address;

import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.in.address.query.LocationView;

import java.util.Optional;

public interface AddressPortIn {
    Optional<AddressView> loadAddressView(Integer addressId);
    Optional<LocationView> loadCityView(Integer cityId);
}
