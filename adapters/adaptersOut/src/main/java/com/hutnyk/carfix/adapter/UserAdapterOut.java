package com.hutnyk.carfix.adapter;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.mapper.UserMapper;
import com.hutnyk.carfix.out.UserPortOut;
import com.hutnyk.carfix.repository.UserRepository;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@PersistenceAdapter
public class UserAdapterOut implements UserPortOut {

    private final UserRepository userRepository;

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
}
