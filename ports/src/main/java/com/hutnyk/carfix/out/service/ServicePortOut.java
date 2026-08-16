package com.hutnyk.carfix.out.service;

import com.hutnyk.carfix.service.Service;

public interface ServicePortOut {

    /** Persists the service, its bay-type links and every requirement slot; ids are filled in on the result. */
    Service insert(Service service);
}
