package com.hutnyk.carfix.address;

import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.address.AddressPortIn;
import com.hutnyk.carfix.in.address.query.AddressView;
import com.hutnyk.carfix.out.address.AddressPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@ApplicationService
@RequiredArgsConstructor
public class AddressService implements AddressPortIn {

    private final AddressPortOut addressPortOut;

    @Override
    @Transactional(readOnly = true)
    public Optional<AddressView> loadAddressView(Integer addressId) {
        return addressPortOut.loadView(addressId);
    }
}
