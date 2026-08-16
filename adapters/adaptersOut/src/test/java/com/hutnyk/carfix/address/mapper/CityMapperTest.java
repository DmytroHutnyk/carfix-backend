package com.hutnyk.carfix.address.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.address.City;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.address.entity.CountryEntity;
import com.hutnyk.carfix.address.entity.RegionEntity;
import com.hutnyk.carfix.in.address.query.LocationView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class CityMapperTest {

    private static final BigDecimal WARSAW_LAT = new BigDecimal("52.229700");
    private static final BigDecimal WARSAW_LNG = new BigDecimal("21.012200");

    private static RegionEntity masovian() {
        CountryEntity poland = new CountryEntity();
        poland.setIso(CountryIso.PL);
        poland.setName("Poland");

        RegionEntity masovian = new RegionEntity();
        masovian.setId(3);
        masovian.setName("Masovian Voivodeship");
        masovian.setCountryEntity(poland);
        return masovian;
    }

    @Test
    public void test_round_trip_keeps_the_id_the_name_and_the_region_reference() {
        //given
        City city = City.of(11, "Warsaw", 3, null, null);
        RegionEntity region = masovian();

        //when
        CityEntity entity = CityMapper.toEntity(city, region);
        City result = CityMapper.toDomain(entity);

        //then
        assertThat(entity.getId()).isEqualTo(11);
        assertThat(entity.getName()).isEqualTo("Warsaw");
        assertThat(entity.getRegionEntity()).isSameAs(region);
        assertThat(result.getId()).isEqualTo(11);
        assertThat(result.getName()).isEqualTo("Warsaw");
        assertThat(result.getRegionId()).isEqualTo(3);
        assertThat(result.hasCoordinates()).isFalse();
    }

    @Test
    public void test_round_trip_keeps_the_coordinates() {
        //given
        City city = City.of(11, "Warsaw", 3, WARSAW_LAT, WARSAW_LNG);

        //when
        CityEntity entity = CityMapper.toEntity(city, masovian());
        City result = CityMapper.toDomain(entity);

        //then
        assertThat(entity.getLatitude()).isEqualByComparingTo(WARSAW_LAT);
        assertThat(entity.getLongitude()).isEqualByComparingTo(WARSAW_LNG);
        assertThat(result.getLatitude()).isEqualByComparingTo(WARSAW_LAT);
        assertThat(result.getLongitude()).isEqualByComparingTo(WARSAW_LNG);
    }

    @Test
    public void test_toView_flattens_the_region_and_the_country() {
        //given
        CityEntity entity = CityMapper.toEntity(City.of(11, "Warsaw", 3, WARSAW_LAT, WARSAW_LNG), masovian());

        //when
        LocationView result = CityMapper.toView(entity);

        //then
        assertThat(result.cityId()).isEqualTo(11);
        assertThat(result.city()).isEqualTo("Warsaw");
        assertThat(result.region()).isEqualTo("Masovian Voivodeship");
        assertThat(result.countryIso()).isEqualTo(CountryIso.PL);
        assertThat(result.latitude()).isEqualByComparingTo(WARSAW_LAT);
        assertThat(result.longitude()).isEqualByComparingTo(WARSAW_LNG);
    }

    @Test
    public void test_updateEntity_copies_the_name_the_region_and_the_coordinates() {
        //given
        CityEntity entity = CityMapper.toEntity(City.of(11, "Warsaw", 3, null, null), masovian());
        RegionEntity region = masovian();

        //when
        CityMapper.updateEntity(entity, City.of(11, "Warszawa", 3, WARSAW_LAT, WARSAW_LNG), region);

        //then
        assertThat(entity.getId()).isEqualTo(11);
        assertThat(entity.getName()).isEqualTo("Warszawa");
        assertThat(entity.getRegionEntity()).isSameAs(region);
        assertThat(entity.getLatitude()).isEqualByComparingTo(WARSAW_LAT);
        assertThat(entity.getLongitude()).isEqualByComparingTo(WARSAW_LNG);
    }

    @Test
    public void test_null_guards() {
        //when + then
        assertThat(CityMapper.toDomain(null)).isNull();
        assertThat(CityMapper.toEntity(null, masovian())).isNull();
        assertThat(CityMapper.toView(null)).isNull();
    }
}
