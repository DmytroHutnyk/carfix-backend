package com.hutnyk.carfix.address;

import com.hutnyk.carfix.out.address.AddressPortOut;

/**
 * Finds the city row an address should point at, inserting the region and/or city when the
 * Google-English names are not stored yet. Plain class, no Spring, constructed by each service
 * around its {@link AddressPortOut}.
 */
public final class CityResolver {

    private final AddressPortOut addressPortOut;

    public CityResolver(AddressPortOut addressPortOut) {
        this.addressPortOut = addressPortOut;
    }

    public Integer resolveCityId(String cityName, String regionName, CountryIso countryIso) {
        Region region = addressPortOut.findRegion(regionName, countryIso)
                .orElseGet(() -> addressPortOut.insertRegion(Region.of(null, regionName, countryIso)));
        City city = addressPortOut.findCity(cityName, region.getId())
                .orElseGet(() -> addressPortOut.insertCity(City.of(null, cityName, region.getId())));
        return city.getId();
    }
}
