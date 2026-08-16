package com.hutnyk.carfix.in.owner;

import com.hutnyk.carfix.owner.Owner;

import java.util.Optional;

public interface OwnerPortIn {

    Optional<Owner> loadByOwnerUsername(String email);
}
