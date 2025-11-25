package com.hutnyk.carfix;

import com.hutnyk.carfix.in.UserPortIn;
import com.hutnyk.carfix.out.UserPortOut;
import com.hutnyk.carfix.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserService implements UserPortIn {

    private final UserPortOut portOut;

    @Override
    public Optional<User> loadUserByEmail(String email) {
        return portOut.loadUserByEmail(email);
    }
}
