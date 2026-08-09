package com.hutnyk.carfix.role.mapper;

import com.hutnyk.carfix.role.Role;
import com.hutnyk.carfix.role.entity.RoleEntity;

public class RoleMapper {

    public static Role toDomain(RoleEntity e) {
        if (e == null) return null;
        return Role.of(e.getId(), e.getName());
    }
}
