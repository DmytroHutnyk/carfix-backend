package com.hutnyk.carfix.address.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.Region;
import com.hutnyk.carfix.address.entity.CountryEntity;
import com.hutnyk.carfix.address.entity.RegionEntity;
import org.junit.jupiter.api.Test;

public class RegionMapperTest {

    private static CountryEntity poland() {
        CountryEntity poland = new CountryEntity();
        poland.setIso(CountryIso.PL);
        poland.setName("Poland");
        return poland;
    }

    @Test
    public void test_round_trip_keeps_the_id_the_name_and_the_country_reference() {
        Region region = Region.of(3, "Masovian Voivodeship", CountryIso.PL);
        CountryEntity country = poland();

        RegionEntity entity = RegionMapper.toEntity(region, country);
        Region result = RegionMapper.toDomain(entity);

        assertThat(entity.getId()).isEqualTo(3);
        assertThat(entity.getName()).isEqualTo("Masovian Voivodeship");
        assertThat(entity.getCountryEntity()).isSameAs(country);
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("Masovian Voivodeship");
        assertThat(result.getCountryIso()).isEqualTo(CountryIso.PL);
    }

    @Test
    public void test_null_guards() {
        assertThat(RegionMapper.toDomain(null)).isNull();
        assertThat(RegionMapper.toEntity(null, poland())).isNull();
    }
}
