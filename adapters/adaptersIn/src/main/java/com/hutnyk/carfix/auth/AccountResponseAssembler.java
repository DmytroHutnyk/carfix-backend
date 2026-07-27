package com.hutnyk.carfix.auth;

import com.hutnyk.carfix.auth.dto.response.AccountResponse;
import com.hutnyk.carfix.auth.exception.CustomerNotFoundException;
import com.hutnyk.carfix.in.customer.CustomerPortIn;
import com.hutnyk.carfix.auth.mapper.LoginUserMapper;
import com.hutnyk.carfix.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountResponseAssembler {

    private final CustomerPortIn customerPortIn;
    private final LoginUserMapper loginUserMapper;

    public AccountResponse assemble(String username, UserRole role) {
        return switch (role) {
            case CUSTOMER -> loginUserMapper.customerToAccountResponse(
                    customerPortIn.loadByCustomerUsername(username).orElseThrow(() ->
                            new CustomerNotFoundException("No customer aggregate for authenticated principal: " + username)));
            // case OWNER -> ownerAccountMapper.toResponse(ownerPortIn.loadByOwnerUsername(username)...);  // add WITH the owner slice — out of scope now
            default -> throw new IllegalStateException("Unsupported account role for assembly: " + role);
        };
    }
}
