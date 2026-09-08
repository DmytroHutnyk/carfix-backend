package com.hutnyk.carfix.branch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class CancellationPolicyTest {

    @Test
    public void test_maps_policies_to_notice_windows() {
        //then
        assertThat(CancellationPolicy.STRICT.notice()).isEqualTo(Duration.ofHours(48));
        assertThat(CancellationPolicy.MODERATE.notice()).isEqualTo(Duration.ofHours(24));
        assertThat(CancellationPolicy.FLEXIBLE.notice()).isEqualTo(Duration.ofHours(2));
    }
}
