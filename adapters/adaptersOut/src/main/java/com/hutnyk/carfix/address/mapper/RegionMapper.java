package com.hutnyk.carfix.address.mapper;

import com.hutnyk.carfix.address.Region;
import com.hutnyk.carfix.address.entity.CountryEntity;
import com.hutnyk.carfix.address.entity.RegionEntity;

public final class RegionMapper {

    private RegionMapper() {
    }

    public static Region toDomain(RegionEntity entity) {
        if (entity == null) {
            return null;
        }
        return Region.of(entity.getId(), entity.getName(), entity.getCountryEntity().getIso());
    }

    public static RegionEntity toEntity(Region region, CountryEntity country) {
        if (region == null) {
            return null;
        }
        RegionEntity entity = new RegionEntity();
        entity.setId(region.getId());
        entity.setName(region.getName());
        entity.setCountryEntity(country);
        return entity;
    }
}
