package com.hutnyk.carfix.out.owner;

import com.hutnyk.carfix.owner.Owner;

import java.util.Optional;

public interface OwnerPortOut {
    Optional<Owner> loadOwnerByUsername(String email);
}
