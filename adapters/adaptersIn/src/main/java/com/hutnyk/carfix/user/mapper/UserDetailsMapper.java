package com.hutnyk.carfix.user.mapper;

import com.hutnyk.carfix.security.CustomUserDetails;
import com.hutnyk.carfix.user.User;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsMapper {
    public static UserDetails userToUserDetails(User user){
        return new CustomUserDetails(
                user.getEmail(),
                user.getPasswordHash().getValue(),
                user.getRole()
        );
    }
}
