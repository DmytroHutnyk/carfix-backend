package com.hutnyk.carfix.out;


import com.hutnyk.carfix.user.User;

import java.util.Optional;

public interface UserPortOut {

    Optional<User> loadUserByEmail(String email);
}
