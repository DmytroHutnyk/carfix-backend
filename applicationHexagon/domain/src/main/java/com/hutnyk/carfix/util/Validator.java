package com.hutnyk.carfix.util;

import com.hutnyk.carfix.InvalidDomainObjectError;

public class Validator {
    public static <T> T notNull(T value){
        if(value == null){
            throw new InvalidDomainObjectError("Null is not allowed");
        }
        return value;
    }

    public static String notEmpty(String value){
        notNull(value);

        if(value.isBlank()){
            throw new InvalidDomainObjectError("String can not be empty or blank");
        }
        return value;
    }
}
