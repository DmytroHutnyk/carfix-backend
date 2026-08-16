package com.hutnyk.carfix.owner.mapper;

import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.owner.entity.OwnerEntity;
import com.hutnyk.carfix.user.mapper.UserMapper;

public class OwnerMapper {

    public static Owner toDomain(OwnerEntity entity) {
        if (entity == null) {
            return null;
        }
        return Owner.of(
                UserMapper.toDomain(entity.getUserEntity()),
                entity.getBusinessName(),
                entity.getVatIn(),
                entity.getRegon());
    }
}
