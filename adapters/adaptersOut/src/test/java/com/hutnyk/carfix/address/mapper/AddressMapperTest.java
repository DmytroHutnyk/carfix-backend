package com.hutnyk.carfix.address.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.address.entity.CountryEntity;
import com.hutnyk.carfix.address.entity.RegionEntity;
import com.hutnyk.carfix.in.address.query.AddressView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class AddressMapperTest {

    private static CityEntity warsaw() {
        CountryEntity poland = new CountryEntity();
        poland.setIso(CountryIso.PL);
        poland.setName("Poland");

        RegionEntity masovian = new RegionEntity();
        masovian.setId(3);
        masovian.setName("Masovian Voivodeship");
        masovian.setCountryEntity(poland);

        CityEntity warsaw = new CityEntity();
        warsaw.setId(11);
        warsaw.setName("Warsaw");
        warsaw.setRegionEntity(masovian);
        return warsaw;
    }

    private static AddressEntity addressEntity() {
        AddressEntity entity = new AddressEntity();
        entity.setId(5);
        entity.setStreetName("Marszałkowska");
        entity.setBuildingNumber("10");
        entity.setFlatNumber("3A");
        entity.setPostalCode("00-001");
        entity.setLatitude(new BigDecimal("52.229700"));
        entity.setLongitude(new BigDecimal("21.012200"));
        entity.setGooglePlaceId("ChIJ_place");
        entity.setCityEntity(warsaw());
        return entity;
    }

    @Test
    public void test_toView_flattens_the_city_region_country_chain() {
        AddressView view = AddressMapper.toView(addressEntity());

        assertThat(view.id()).isEqualTo(5);
        assertThat(view.streetName()).isEqualTo("Marszałkowska");
        assertThat(view.buildingNumber()).isEqualTo("10");
        assertThat(view.flatNumber()).isEqualTo("3A");
        assertThat(view.postalCode()).isEqualTo("00-001");
        assertThat(view.city()).isEqualTo("Warsaw");
        assertThat(view.region()).isEqualTo("Masovian Voivodeship");
        assertThat(view.countryIso()).isEqualTo(CountryIso.PL);
        assertThat(view.countryName()).isEqualTo("Poland");
        assertThat(view.latitude()).isEqualByComparingTo("52.229700");
        assertThat(view.longitude()).isEqualByComparingTo("21.012200");
        assertThat(view.googlePlaceId()).isEqualTo("ChIJ_place");
    }

    @Test
    public void test_toDomain_keeps_the_city_id_only() {
        Address address = AddressMapper.toDomain(addressEntity());

        assertThat(address.getId()).isEqualTo(5);
        assertThat(address.getCityId()).isEqualTo(11);
        assertThat(address.getFlatNumber()).isEqualTo("3A");
    }

    @Test
    public void test_toEntity_copies_every_field_and_the_city_reference() {
        Address address = Address.of(null, "Marszałkowska", "10", null, "00-001", 11,
                new BigDecimal("52.229700"), new BigDecimal("21.012200"), "ChIJ_place");
        CityEntity city = warsaw();

        AddressEntity entity = AddressMapper.toEntity(address, city);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getStreetName()).isEqualTo("Marszałkowska");
        assertThat(entity.getBuildingNumber()).isEqualTo("10");
        assertThat(entity.getFlatNumber()).isNull();
        assertThat(entity.getPostalCode()).isEqualTo("00-001");
        assertThat(entity.getLatitude()).isEqualByComparingTo("52.229700");
        assertThat(entity.getLongitude()).isEqualByComparingTo("21.012200");
        assertThat(entity.getGooglePlaceId()).isEqualTo("ChIJ_place");
        assertThat(entity.getCityEntity()).isSameAs(city);
    }

    @Test
    public void test_updateEntity_replaces_fields_and_keeps_the_id() {
        AddressEntity entity = addressEntity();
        CityEntity krakow = new CityEntity();
        krakow.setId(12);
        krakow.setName("Kraków");
        Address address = Address.of(5, "Floriańska", "1", null, "31-019", 12, null, null, null);

        AddressMapper.updateEntity(entity, address, krakow);

        assertThat(entity.getId()).isEqualTo(5);
        assertThat(entity.getStreetName()).isEqualTo("Floriańska");
        assertThat(entity.getBuildingNumber()).isEqualTo("1");
        assertThat(entity.getFlatNumber()).isNull();
        assertThat(entity.getPostalCode()).isEqualTo("31-019");
        assertThat(entity.getLatitude()).isNull();
        assertThat(entity.getLongitude()).isNull();
        assertThat(entity.getGooglePlaceId()).isNull();
        assertThat(entity.getCityEntity()).isSameAs(krakow);
    }

    @Test
    public void test_null_guards() {
        assertThat(AddressMapper.toView(null)).isNull();
        assertThat(AddressMapper.toDomain(null)).isNull();
        assertThat(AddressMapper.toEntity(null, warsaw())).isNull();
    }
}
