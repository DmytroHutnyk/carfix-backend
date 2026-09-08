package com.hutnyk.carfix.owner;

import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Owner {

    @EqualsAndHashCode.Include
    private final User user;
    private final String businessName;
    private final String vatIn;
    private final String regon;

    @Builder
    private Owner(User user, String businessName, String vatIn, String regon) {
        this.user = Validator.notNull(user, "user");
        this.businessName = Validator.notBlank(businessName, "businessName");
        this.vatIn = Validator.notBlank(vatIn, "vatIn");
        this.regon = Validator.notBlank(regon, "regon");
    }

    public static Owner of(User user, String businessName, String vatIn, String regon) {
        return Owner.builder()
                .user(user)
                .businessName(businessName)
                .vatIn(vatIn)
                .regon(regon)
                .build();
    }
}
