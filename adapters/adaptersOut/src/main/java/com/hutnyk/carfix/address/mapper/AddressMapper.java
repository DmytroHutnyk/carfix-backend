package com.hutnyk.carfix.address.mapper;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.address.entity.CountryEntity;
import com.hutnyk.carfix.address.entity.RegionEntity;
import com.hutnyk.carfix.in.address.query.AddressView;

public final class AddressMapper {

    private AddressMapper() {
    }

    public static AddressView toView(AddressEntity entity) {
        if (entity == null) {
            return null;
        }
        CityEntity city = entity.getCityEntity();
        RegionEntity region = city.getRegionEntity();
        CountryEntity country = region.getCountryEntity();
        return new AddressView(
                entity.getId(),
                entity.getStreetName(),
                entity.getBuildingNumber(),
                entity.getFlatNumber(),
                entity.getPostalCode(),
                city.getName(),
                region.getName(),
                country.getIso(),
                country.getName(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getGooglePlaceId());
    }

    public static Address toDomain(AddressEntity entity) {
        if (entity == null) {
            return null;
        }
        return Address.of(
                entity.getId(),
                entity.getStreetName(),
                entity.getBuildingNumber(),
                entity.getFlatNumber(),
                entity.getPostalCode(),
                entity.getCityEntity().getId(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getGooglePlaceId());
    }

    public static AddressEntity toEntity(Address address, CityEntity city) {
        if (address == null) {
            return null;
        }
        AddressEntity entity = new AddressEntity();
        entity.setId(address.getId());
        copyFields(entity, address, city);
        return entity;
    }

    public static void updateEntity(AddressEntity entity, Address address, CityEntity city) {
        copyFields(entity, address, city);
    }

    private static void copyFields(AddressEntity entity, Address address, CityEntity city) {
        entity.setStreetName(address.getStreetName());
        entity.setBuildingNumber(address.getBuildingNumber());
        entity.setFlatNumber(address.getFlatNumber());
        entity.setPostalCode(address.getPostalCode());
        entity.setLatitude(address.getLatitude());
        entity.setLongitude(address.getLongitude());
        entity.setGooglePlaceId(address.getGooglePlaceId());
        entity.setCityEntity(city);
    }
}
