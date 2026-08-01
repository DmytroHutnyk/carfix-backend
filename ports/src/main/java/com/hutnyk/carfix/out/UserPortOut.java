package com.hutnyk.carfix.out;

import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;

import java.util.Optional;

public interface UserPortOut {
    Optional<User> loadUserByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
    User update(User user);
}
