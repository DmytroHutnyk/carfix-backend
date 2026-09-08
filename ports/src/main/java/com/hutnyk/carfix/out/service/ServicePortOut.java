package com.hutnyk.carfix.out.service;

import com.hutnyk.carfix.service.Service;

import java.util.Collection;
import java.util.List;

public interface ServicePortOut {

    // Missing ids are omitted; result order need not match input order.
    List<Service> loadByIds(Collection<Integer> serviceIds);

    Service insert(Service service);
}
