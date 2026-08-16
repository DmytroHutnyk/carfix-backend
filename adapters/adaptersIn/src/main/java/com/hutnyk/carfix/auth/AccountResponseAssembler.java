package com.hutnyk.carfix.auth;

import com.hutnyk.carfix.auth.dto.response.AccountResponse;
import com.hutnyk.carfix.auth.mapper.CustomerToResponseMapper;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.customer.CustomerPortIn;
import com.hutnyk.carfix.user.UserResponseAssembler;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountResponseAssembler {

    private final CustomerPortIn customerPortIn;
    private final UserResponseAssembler userResponseAssembler;

    public AccountResponse assemble(String username, UserRole role) {
        return switch (role) {
            case CUSTOMER -> {
                Customer customer = customerPortIn.loadByCustomerUsername(username).orElseThrow(() ->
                        AuthenticatedUserMissingException.noCustomerAggregate(username));
                yield CustomerToResponseMapper.toResponse(customer, userResponseAssembler.toCoreResponse(customer.getUser()));
            }
            // case OWNER -> ownerAccountMapper.toResponse(ownerPortIn.loadByOwnerUsername(username)...);  // add WITH the owner slice — out of scope now
            default -> throw new UnexpectedStateException("Unsupported account role for assembly: " + role);
        };
    }
}
