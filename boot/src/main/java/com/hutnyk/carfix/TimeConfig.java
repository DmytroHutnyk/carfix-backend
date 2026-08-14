package com.hutnyk.carfix;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    /**
     * Zone-neutral on purpose: every consumer re-zones this clock to the branch's own
     * {@code tz} before reading a date or time, so pinning a country here would only mislead.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
