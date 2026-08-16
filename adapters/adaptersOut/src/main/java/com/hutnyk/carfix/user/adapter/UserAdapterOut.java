package com.hutnyk.carfix.user.adapter;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.address.repository.AddressRepository;
import com.hutnyk.carfix.address.repository.CityRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.entity.UserEntity;
import com.hutnyk.carfix.user.mapper.UserMapper;
import com.hutnyk.carfix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@PersistenceAdapter
public class UserAdapterOut implements UserPortOut {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;

    @Override
    public Optional<User> loadUserByEmail(String email){
        return Optional.ofNullable(UserMapper.toDomain(userRepository.getUserByEmail(email)));
    }

    @Override
    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    };

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber){
        return userRepository.existsByPhoneCountryCodeAndPhoneNumber(phoneNumber.countryCode(), phoneNumber.phoneNumber());
    }

    @Override
    public User update(User user){
        UserEntity entity = userRepository.findById(user.getId().id())
                .orElseThrow(() -> new UnexpectedStateException("User not found: " + user.getId().id()));

        AddressEntity address = user.getAddressId() == null
                ? null
                : addressRepository.getReferenceById(user.getAddressId());
        CityEntity preferredCity = user.getPreferredCityId() == null
                ? null
                : cityRepository.getReferenceById(user.getPreferredCityId());
        UserMapper.updateEntity(entity, user, address, preferredCity);

        return UserMapper.toDomain(userRepository.save(entity));
    }
}
