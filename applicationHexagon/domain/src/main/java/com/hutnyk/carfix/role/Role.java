package com.hutnyk.carfix.role;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Role {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;

    //Nullable — platform (seeded) roles belong to no branch
    private final BranchId branchId;

    @Builder
    private Role(Integer id, String name, BranchId branchId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.branchId = branchId;
    }

    public static Role of(Integer id, String name, BranchId branchId) {
        return Role.builder()
                .id(id)
                .name(name)
                .branchId(branchId)
                .build();
    }

    public static Role create(String name, BranchId branchId) {
        return of(null, name, Validator.notNull(branchId, "branchId"));
    }
}
