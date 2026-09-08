package com.hutnyk.carfix;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    // Consumers re-zone this clock to each branch before reading local date or time.
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
