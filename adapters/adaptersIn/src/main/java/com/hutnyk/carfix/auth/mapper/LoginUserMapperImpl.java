package com.hutnyk.carfix.auth.mapper;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.auth.dto.response.CustomerAccountResponse;
import com.hutnyk.carfix.auth.dto.response.OwnerAccountResponse;
import com.hutnyk.carfix.auth.mapper.LoginUserMapper;
import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.mapper.UserToResponseMapper;
import org.springframework.stereotype.Service;

@Service
public class LoginUserMapperImpl implements LoginUserMapper {

    @Override
    public CustomerAccountResponse customerToAccountResponse(Customer customer) {
        User user = customer.getUser();
        return new CustomerAccountResponse(
                user.getRole(),
                UserToResponseMapper.toCoreResponse(user),
                customer.getStatus()
        );
    }

    @Override
    public OwnerAccountResponse ownerToAccountResponse(Owner owner) {
        User user = owner.getUser();
        return new OwnerAccountResponse(
                user.getRole(),
                UserToResponseMapper.toCoreResponse(user),
                owner.getBusinessName(),
                owner.getVatIn(),
                owner.getRegon()
        );
    }
}
