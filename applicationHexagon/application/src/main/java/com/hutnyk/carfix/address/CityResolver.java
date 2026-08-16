package com.hutnyk.carfix.address;

import com.hutnyk.carfix.out.address.AddressPortOut;

import java.math.BigDecimal;

/**
 * Finds the city row an address or a preferred location should point at, inserting the region
 * and/or city when the Google-English names are not stored yet. Coordinates are the city centre
 * of a preferred-location pick: stored on insert, filled in when the existing row has none, never
 * overwritten. Plain class, no Spring, constructed by each service around its {@link AddressPortOut}.
 */
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
