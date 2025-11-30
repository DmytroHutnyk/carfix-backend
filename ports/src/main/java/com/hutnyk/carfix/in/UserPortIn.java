package com.hutnyk.carfix.in;

import com.hutnyk.carfix.user.User;

import java.util.Optional;

public interface UserPortIn {
    Optional<User> loadUserByEmail(String email);
}
