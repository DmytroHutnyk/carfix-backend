package com.hutnyk.carfix.customer;


import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Customer {

    @EqualsAndHashCode.Include
    private final User user;
    private final CustomerStatus status;

    @Builder
    private Customer(User user, CustomerStatus status) {
        this.user = Validator.notNull(user, "user");
        this.status = Validator.notNull(status, "status");
    }

    public static Customer of(User user, CustomerStatus status){
        return Customer.builder()
                .user(user)
                .status(status)
                .build();
    }
}
