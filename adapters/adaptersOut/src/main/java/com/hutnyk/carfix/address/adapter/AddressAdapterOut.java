package com.hutnyk.carfix.address.adapter;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.City;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.address.Region;
import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.address.entity.CountryEntity;
import com.hutnyk.carfix.address.entity.RegionEntity;
import com.hutnyk.carfix.address.mapper.AddressMapper;
import com.hutnyk.carfix.address.mapper.CityMapper;
import com.hutnyk.carfix.address.mapper.RegionMapper;
import com.hutnyk.carfix.address.repository.AddressRepository;
import com.hutnyk.carfix.address.repository.CityRepository;
import com.hutnyk.carfix.address.repository.CountryRepository;
import com.hutnyk.carfix.address.repository.RegionRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.out.address.AddressPortOut;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@PersistenceAdapter
public class AddressAdapterOut implements AddressPortOut {

    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;

    @Override
    public Optional<AddressView> loadView(Integer addressId) {
        return addressRepository.findWithLocationById(addressId).map(AddressMapper::toView);
    }

    @Override
    public Address insert(Address address) {
        CityEntity city = cityRepository.getReferenceById(address.getCityId());
        return AddressMapper.toDomain(addressRepository.save(AddressMapper.toEntity(address, city)));
    }

    @Override
    public Address update(Address address) {
        AddressEntity entity = addressRepository.findById(address.getId())
                .orElseThrow(() -> new UnexpectedStateException("Address row missing on update: " + address.getId()));
        CityEntity city = cityRepository.getReferenceById(address.getCityId());
        AddressMapper.updateEntity(entity, address, city);
        return AddressMapper.toDomain(addressRepository.save(entity));
    }

    @Override
    public void deleteById(Integer addressId) {
        addressRepository.deleteById(addressId);
    }

    @Override
    public Optional<Region> findRegion(String name, CountryIso countryIso) {
        return regionRepository.findFirstByNameAndCountryEntityIso(name, countryIso).map(RegionMapper::toDomain);
    }

    @Override
    public Region insertRegion(Region region) {
        CountryEntity country = countryRepository.getReferenceById(region.getCountryIso());
        return RegionMapper.toDomain(regionRepository.save(RegionMapper.toEntity(region, country)));
    }

    @Override
    public Optional<City> findCity(String name, Integer regionId) {
        return cityRepository.findFirstByNameAndRegionEntityId(name, regionId).map(CityMapper::toDomain);
    }

    @Override
    public City insertCity(City city) {
        RegionEntity region = regionRepository.getReferenceById(city.getRegionId());
        return CityMapper.toDomain(cityRepository.save(CityMapper.toEntity(city, region)));
    }
}
