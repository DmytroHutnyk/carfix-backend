package com.hutnyk.carfix.address.mapper;

import com.hutnyk.carfix.address.City;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.address.entity.RegionEntity;

public final class CityMapper {

    private CityMapper() {
    }

    public static City toDomain(CityEntity entity) {
        if (entity == null) {
            return null;
        }
        return City.of(entity.getId(), entity.getName(), entity.getRegionEntity().getId());
    }

    public static CityEntity toEntity(City city, RegionEntity region) {
        if (city == null) {
            return null;
        }
        CityEntity entity = new CityEntity();
        entity.setId(city.getId());
        entity.setName(city.getName());
        entity.setRegionEntity(region);
        return entity;
    }
}
