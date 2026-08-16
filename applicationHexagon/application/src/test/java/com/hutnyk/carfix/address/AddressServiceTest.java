package com.hutnyk.carfix.address;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.out.address.AddressPortOut;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class AddressServiceTest {

    private static final AddressView VIEW = new AddressView(5, "Marszałkowska", "10", null, "00-001",
            "Warsaw", "Masovian Voivodeship", CountryIso.PL, "Poland", null, null, null);

    private static final class StubAddressPortOut implements AddressPortOut {
        Integer requestedId;

        @Override
        public Optional<AddressView> loadView(Integer addressId) {
            this.requestedId = addressId;
            return addressId == 5 ? Optional.of(VIEW) : Optional.empty();
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
            throw new AssertionError("not used");
        }

        @Override
        public Region insertRegion(Region region) {
            throw new AssertionError("not used");
        }

        @Override
        public Optional<City> findCity(String name, Integer regionId) {
            throw new AssertionError("not used");
        }

        @Override
        public City insertCity(City city) {
            throw new AssertionError("not used");
        }
    }

    private final StubAddressPortOut portOut = new StubAddressPortOut();
    private final AddressService service = new AddressService(portOut);

    @Test
    public void test_loadAddressView_passes_the_id_through_and_returns_the_view() {
        //when
        Optional<AddressView> result = service.loadAddressView(5);

        //then
        assertThat(portOut.requestedId).isEqualTo(5);
        assertThat(result).contains(VIEW);
    }

    @Test
    public void test_loadAddressView_is_empty_for_an_unknown_id() {
        //when + then
        assertThat(service.loadAddressView(6)).isEmpty();
    }
}
