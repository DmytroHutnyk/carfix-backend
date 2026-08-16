package com.hutnyk.carfix.address;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.out.address.AddressPortOut;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CityResolverTest {

    private static final class RecordingAddressPortOut implements AddressPortOut {
        final List<Region> knownRegions = new ArrayList<>();
        final List<City> knownCities = new ArrayList<>();
        final List<Region> insertedRegions = new ArrayList<>();
        final List<City> insertedCities = new ArrayList<>();
        int nextId = 100;

        @Override
        public Optional<AddressView> loadView(Integer addressId) {
            return Optional.empty();
        }

        @Override
        public Address insert(Address address) {
            throw new AssertionError("not used");
        }

        @Override
        public Address update(Address address) {
            throw new AssertionError("not used");
        }

        @Override
        public void deleteById(Integer addressId) {
            throw new AssertionError("not used");
        }

        @Override
        public Optional<Region> findRegion(String name, CountryIso countryIso) {
            return knownRegions.stream()
                    .filter(r -> r.getName().equals(name) && r.getCountryIso() == countryIso)
                    .findFirst();
        }

        @Override
        public Region insertRegion(Region region) {
            Region saved = Region.of(nextId++, region.getName(), region.getCountryIso());
            insertedRegions.add(saved);
            knownRegions.add(saved);
            return saved;
        }

        @Override
        public Optional<City> findCity(String name, Integer regionId) {
            return knownCities.stream()
                    .filter(c -> c.getName().equals(name) && c.getRegionId().equals(regionId))
                    .findFirst();
        }

        @Override
        public City insertCity(City city) {
            City saved = City.of(nextId++, city.getName(), city.getRegionId());
            insertedCities.add(saved);
            knownCities.add(saved);
            return saved;
        }
    }

    private final RecordingAddressPortOut portOut = new RecordingAddressPortOut();
    private final CityResolver resolver = new CityResolver(portOut);

    @Test
    public void test_existing_region_and_city_are_reused_without_inserts() {
        //given
        portOut.knownRegions.add(Region.of(3, "Masovian Voivodeship", CountryIso.PL));
        portOut.knownCities.add(City.of(11, "Warsaw", 3));

        //when
        Integer cityId = resolver.resolveCityId("Warsaw", "Masovian Voivodeship", CountryIso.PL);

        //then
        assertThat(cityId).isEqualTo(11);
        assertThat(portOut.insertedRegions).isEmpty();
        assertThat(portOut.insertedCities).isEmpty();
    }

    @Test
    public void test_unknown_region_inserts_region_then_city_under_it() {
        //when
        Integer cityId = resolver.resolveCityId("Berlin", "Berlin", CountryIso.DE);

        //then
        assertThat(portOut.insertedRegions).hasSize(1);
        assertThat(portOut.insertedRegions.get(0).getName()).isEqualTo("Berlin");
        assertThat(portOut.insertedRegions.get(0).getCountryIso()).isEqualTo(CountryIso.DE);
        assertThat(portOut.insertedCities).hasSize(1);
        assertThat(portOut.insertedCities.get(0).getRegionId()).isEqualTo(portOut.insertedRegions.get(0).getId());
        assertThat(cityId).isEqualTo(portOut.insertedCities.get(0).getId());
    }

    @Test
    public void test_known_region_with_unknown_city_inserts_only_the_city() {
        //given
        portOut.knownRegions.add(Region.of(3, "Masovian Voivodeship", CountryIso.PL));

        //when
        Integer cityId = resolver.resolveCityId("Radom", "Masovian Voivodeship", CountryIso.PL);

        //then
        assertThat(portOut.insertedRegions).isEmpty();
        assertThat(portOut.insertedCities).hasSize(1);
        assertThat(portOut.insertedCities.get(0).getName()).isEqualTo("Radom");
        assertThat(portOut.insertedCities.get(0).getRegionId()).isEqualTo(3);
        assertThat(cityId).isEqualTo(portOut.insertedCities.get(0).getId());
    }

    @Test
    public void test_same_city_name_in_another_country_is_a_different_row() {
        //given
        portOut.knownRegions.add(Region.of(3, "Masovian Voivodeship", CountryIso.PL));
        portOut.knownCities.add(City.of(11, "Warsaw", 3));

        //when
        Integer cityId = resolver.resolveCityId("Warsaw", "Indiana", CountryIso.US);

        //then
        assertThat(cityId).isNotEqualTo(11);
        assertThat(portOut.insertedRegions).hasSize(1);
        assertThat(portOut.insertedCities).hasSize(1);
    }
}
