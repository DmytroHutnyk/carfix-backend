package com.hutnyk.carfix.user;

import com.hutnyk.carfix.util.Validator;
import java.util.UUID;

public record UserId(UUID id) {
    public UserId{
        Validator.notNull(id);
    }

    public static UserId of(UUID id){
        return new UserId(id);
    }

    public static UserId genId(){
        return new UserId(UUID.randomUUID());
    }
}
