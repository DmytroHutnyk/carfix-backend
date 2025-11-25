package com.hutnyk.carfix;

import com.hutnyk.carfix.out.UserPortOut;
import com.hutnyk.carfix.repository.UserRepository;
import com.hutnyk.carfix.user.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@PersistenceAdapter
public class UserAdapterOut implements UserPortOut {

    private final UserRepository userRepository;

    @Override
    public Optional<User> loadUserByEmail(String email){
        return Optional.ofNullable(userRepository.getUserByEmail(email));
    }
}
