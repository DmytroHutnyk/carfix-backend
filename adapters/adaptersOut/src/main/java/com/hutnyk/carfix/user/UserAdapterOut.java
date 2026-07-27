package com.hutnyk.carfix.user;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.user.UserPortOut;
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

    @Override
    public User update(User user){
        UserEntity entity = userRepository.findById(user.getId().id())
                .orElseThrow(() -> new IllegalStateException("User not found: " + user.getId().id()));

        entity.setName(user.getName());
        entity.setSurname(user.getSurname());
        entity.setDateOfBirth(user.getDateOfBirth());

        return UserMapper.toDomain(userRepository.save(entity));
    }
}
