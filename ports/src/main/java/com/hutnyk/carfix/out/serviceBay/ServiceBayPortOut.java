package com.hutnyk.carfix.out.serviceBay;

import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayType;

public interface ServiceBayPortOut {
    ServiceBayType insertType(ServiceBayType type);
    ServiceBay insert(ServiceBay bay);
}
