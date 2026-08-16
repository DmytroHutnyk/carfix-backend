package com.hutnyk.carfix.owner.mapper;

import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.owner.entity.OwnerEntity;
import com.hutnyk.carfix.user.User;

public final class OwnerMapper {

    private OwnerMapper() {
    }

    public static Owner toDomain(OwnerEntity entity, User user) {
        if (entity == null) {
            return null;
        }
        return Owner.of(user, entity.getBusinessName(), entity.getVatIn(), entity.getRegon());
    }
}
