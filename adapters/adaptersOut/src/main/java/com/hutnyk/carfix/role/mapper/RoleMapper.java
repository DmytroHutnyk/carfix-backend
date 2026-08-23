package com.hutnyk.carfix.role.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.role.Role;
import com.hutnyk.carfix.role.entity.RoleEntity;

public class RoleMapper {

    public static Role toDomain(RoleEntity e) {
        if (e == null) return null;
        return Role.of(
                e.getId(),
                e.getName(),
                e.getBranchEntity() == null ? null : BranchId.of(e.getBranchEntity().getId()));
    }

    public static RoleEntity toEntity(Role role, BranchEntity branch) {
        if (role == null) return null;
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setBranchEntity(branch);
        return entity;
    }
}
