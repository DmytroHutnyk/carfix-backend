package com.hutnyk.carfix.owner.adapter;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.owner.OwnerPortOut;
import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.owner.mapper.OwnerMapper;
import com.hutnyk.carfix.owner.repository.OwnerRepository;
import com.hutnyk.carfix.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@PersistenceAdapter
public class OwnerAdapterOut implements OwnerPortOut {

    private final OwnerRepository ownerRepository;

    @Override
    public Optional<Owner> findOwnerByUsername(String email) {
        return ownerRepository.findWithUserByEmail(email)
                .map(entity -> OwnerMapper.toDomain(entity, UserMapper.toDomain(entity.getUserEntity())));
    }
}
