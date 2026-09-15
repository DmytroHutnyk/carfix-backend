package com.hutnyk.carfix.role.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class RoleNotFoundException extends NotFoundException {

    public RoleNotFoundException(String name) {
        super(RoleErrorCode.ROLE_NOT_FOUND, "Role", name);
    }
}
