package com.hutnyk.carfix.owner;

import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.owner.OwnerPortIn;
import com.hutnyk.carfix.out.owner.OwnerPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@ApplicationService
public class OwnerService implements OwnerPortIn {

    private final OwnerPortOut ownerPortOut;

    @Override
    @Transactional(readOnly = true)
    public Optional<Owner> loadByOwnerUsername(String email) {
        return ownerPortOut.findOwnerByUsername(email);
    }
}
