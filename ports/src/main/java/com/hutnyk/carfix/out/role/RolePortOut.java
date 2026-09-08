package com.hutnyk.carfix.out.role;

import com.hutnyk.carfix.role.Role;

import java.util.List;
import java.util.UUID;

public interface RolePortOut {
    Role insert(Role role);

    List<Role> findAllForBranch(UUID branchId);
}
