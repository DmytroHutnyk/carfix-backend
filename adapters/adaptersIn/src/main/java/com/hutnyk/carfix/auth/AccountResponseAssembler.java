package com.hutnyk.carfix.auth;

import com.hutnyk.carfix.auth.dto.response.AccountResponse;
import com.hutnyk.carfix.auth.mapper.CustomerToResponseMapper;
import com.hutnyk.carfix.auth.mapper.OwnerToResponseMapper;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.customer.CustomerPortIn;
import com.hutnyk.carfix.in.owner.OwnerPortIn;
import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.user.UserResponseAssembler;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountResponseAssembler {

    private final CustomerPortIn customerPortIn;
    private final OwnerPortIn ownerPortIn;
    private final UserResponseAssembler userResponseAssembler;

    public AccountResponse assemble(String username, UserRole role) {
        return switch (role) {
            case CUSTOMER -> {
                Customer customer = customerPortIn.loadByCustomerUsername(username).orElseThrow(() ->
                        AuthenticatedUserMissingException.noCustomerAggregate(username));
                yield CustomerToResponseMapper.toResponse(customer, userResponseAssembler.toCoreResponse(customer.getUser()));
            }
            case OWNER -> {
                Owner owner = ownerPortIn.loadByOwnerUsername(username).orElseThrow(() ->
                        AuthenticatedUserMissingException.noOwnerAggregate(username));
                yield OwnerToResponseMapper.toResponse(owner, userResponseAssembler.toCoreResponse(owner.getUser()));
            }
            default -> throw new UnexpectedStateException("Unsupported account role for assembly: " + role);
        };
    }
}
