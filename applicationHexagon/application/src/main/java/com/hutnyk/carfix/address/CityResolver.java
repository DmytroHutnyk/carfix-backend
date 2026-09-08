package com.hutnyk.carfix.address;

import com.hutnyk.carfix.out.address.AddressPortOut;

import java.math.BigDecimal;

/** Inserts missing location rows and fills absent coordinates without replacing stored ones. */
public final class CityResolver {

    private final AddressPortOut addressPortOut;

    public CityResolver(AddressPortOut addressPortOut) {
        this.addressPortOut = addressPortOut;
    }

    public Integer resolveCityId(String cityName, String regionName, CountryIso countryIso) {
        return resolveCityId(cityName, regionName, countryIso, null, null);
    }

    public Integer resolveCityId(String cityName, String regionName, CountryIso countryIso,
                                 BigDecimal latitude, BigDecimal longitude) {
        Region region = addressPortOut.findRegion(regionName, countryIso)
                .orElseGet(() -> addressPortOut.insertRegion(Region.of(null, regionName, countryIso)));
        City city = addressPortOut.findCity(cityName, region.getId())
                .orElseGet(() -> addressPortOut.insertCity(City.of(null, cityName, region.getId(), latitude, longitude)));
        if (!city.hasCoordinates() && latitude != null) {
            city = addressPortOut.updateCity(City.of(city.getId(), city.getName(), city.getRegionId(), latitude, longitude));
        }
        return city.getId();
    }
}
