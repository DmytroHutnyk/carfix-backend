package com.hutnyk.carfix.components;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation that is equivalent to <code>@Component</code> annotation — marks a driven adapter
 * that delivers notifications (email, later SMS) on behalf of an output port.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface NotificationAdapter {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
