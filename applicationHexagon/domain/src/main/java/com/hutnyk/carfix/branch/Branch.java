package com.hutnyk.carfix.branch;

import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.ZoneId;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Branch {

    @EqualsAndHashCode.Include
    private final BranchId id;
    private final String name;
    private final String phoneNumber;
    private final String email;
    private final BranchStatus status;
    private final ZoneId tz;
    private final Integer addressId;
    private final UserId ownerId;

    @Builder
    private Branch(
            BranchId id,
            String name,
            String phoneNumber,
            String email,
            BranchStatus status,
            ZoneId tz,
            Integer addressId,
            UserId ownerId) {
        this.id = Validator.notNull(id, "id");
        this.name = Validator.notBlank(name, "name");
        this.phoneNumber = Validator.validatePhoneNumber(phoneNumber, "phoneNumber");
        this.email = Validator.validateEmail(email, "email");
        this.status = Validator.notNull(status, "status");
        this.tz = Validator.notNull(tz, "tz");
        this.addressId = Validator.notNull(addressId, "addressId");
        this.ownerId = Validator.notNull(ownerId, "ownerId");
    }

    public static Branch of(
            BranchId id,
            String name,
            String phoneNumber,
            String email,
            BranchStatus status,
            ZoneId tz,
            Integer addressId,
            UserId ownerId) {
        return Branch.builder()
                .id(id)
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .status(status)
                .tz(tz)
                .addressId(addressId)
                .ownerId(ownerId)
                .build();
    }
}
