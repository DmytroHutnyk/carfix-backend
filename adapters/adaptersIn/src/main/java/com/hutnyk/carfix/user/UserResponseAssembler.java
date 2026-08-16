package com.hutnyk.carfix.user;

import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.address.AddressPortIn;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.user.dto.response.UserCoreResponse;
import com.hutnyk.carfix.user.mapper.UserToResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserResponseAssembler {

    private final AddressPortIn addressPortIn;

    public UserCoreResponse toCoreResponse(User user) {
        AddressView address = user.getAddressId() == null
                ? null
                : addressPortIn.loadAddressView(user.getAddressId()).orElseThrow(() ->
                        new UnexpectedStateException("User " + user.getId().id() + " points at a missing address " + user.getAddressId()));
        return UserToResponseMapper.toCoreResponse(user, address);
    }
}
