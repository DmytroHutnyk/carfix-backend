package com.hutnyk.carfix.address.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.address.City;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.address.entity.CountryEntity;
import com.hutnyk.carfix.address.entity.RegionEntity;
import org.junit.jupiter.api.Test;

public class CityMapperTest {

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
        City city = City.of(11, "Warsaw", 3);
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
    }

    @Test
    public void test_null_guards() {
        //when + then
        assertThat(CityMapper.toDomain(null)).isNull();
        assertThat(CityMapper.toEntity(null, masovian())).isNull();
    }
}
