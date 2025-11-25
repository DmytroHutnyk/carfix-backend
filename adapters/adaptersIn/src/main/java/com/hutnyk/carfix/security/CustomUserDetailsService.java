package com.hutnyk.carfix.security;

import com.hutnyk.carfix.in.UserPortIn;
import com.hutnyk.carfix.mapper.UserDetailsMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserPortIn userPortIn;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userPortIn.loadUserByEmail(username)
                .map(UserDetailsMapper::userToUserDetails)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User with email(username): " + username + "does not exist"));

    }
}
