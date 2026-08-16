package com.hutnyk.carfix.auth.mapper;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.auth.dto.response.CustomerAccountResponse;
import com.hutnyk.carfix.auth.dto.response.OwnerAccountResponse;
import com.hutnyk.carfix.owner.Owner;

public interface LoginUserMapper {
    CustomerAccountResponse customerToAccountResponse(Customer customer);
    OwnerAccountResponse ownerToAccountResponse(Owner owner);
}
