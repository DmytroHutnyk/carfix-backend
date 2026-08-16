package com.hutnyk.carfix.address.mapper;

import com.hutnyk.carfix.address.City;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.address.entity.RegionEntity;
import com.hutnyk.carfix.in.address.query.LocationView;

public final class CityMapper {

    private CityMapper() {
    }

    public static City toDomain(CityEntity entity) {
        if (entity == null) {
            return null;
        }
        return City.of(entity.getId(), entity.getName(), entity.getRegionEntity().getId(),
                entity.getLatitude(), entity.getLongitude());
    }

    public static LocationView toView(CityEntity entity) {
        if (entity == null) {
            return null;
        }
        RegionEntity region = entity.getRegionEntity();
        return new LocationView(entity.getId(), entity.getName(), region.getName(),
                region.getCountryEntity().getIso(), entity.getLatitude(), entity.getLongitude());
    }

    public static CityEntity toEntity(City city, RegionEntity region) {
        if (city == null) {
            return null;
        }
        CityEntity entity = new CityEntity();
        entity.setId(city.getId());
        copyFields(entity, city, region);
        return entity;
    }

    public static void updateEntity(CityEntity entity, City city, RegionEntity region) {
        copyFields(entity, city, region);
    }

    private static void copyFields(CityEntity entity, City city, RegionEntity region) {
        entity.setName(city.getName());
        entity.setLatitude(city.getLatitude());
        entity.setLongitude(city.getLongitude());
        entity.setRegionEntity(region);
    }
}
