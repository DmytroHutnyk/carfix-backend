package com.hutnyk.carfix.out.service;

import com.hutnyk.carfix.service.Service;

import java.util.Collection;
import java.util.List;

public interface ServicePortOut {

    /**
     * Returns the found services with their acceptable bay types and requirement slots populated.
     * Ids that match no service are simply absent — the result is not padded and not ordered by input.
     */
    List<Service> loadByIds(Collection<Integer> serviceIds);
}
