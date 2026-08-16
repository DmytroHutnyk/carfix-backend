package com.hutnyk.carfix.out.address;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.City;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.Region;
import com.hutnyk.carfix.in.address.query.AddressView;

import java.util.Optional;

public interface AddressPortOut {
    Optional<AddressView> loadView(Integer addressId);
    Address insert(Address address);
    Address update(Address address);
    void deleteById(Integer addressId);
    Optional<Region> findRegion(String name, CountryIso countryIso);
    Region insertRegion(Region region);
    Optional<City> findCity(String name, Integer regionId);
    City insertCity(City city);
}
