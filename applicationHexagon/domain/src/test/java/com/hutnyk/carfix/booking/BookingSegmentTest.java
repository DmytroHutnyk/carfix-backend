package com.hutnyk.carfix.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

public class BookingSegmentTest {

    @Test
    public void ofKeepsEveryField() {
        BookingSegment s = BookingSegment.of(11, LocalTime.of(9, 0), LocalTime.of(10, 0), new BigDecimal("150.00"));

        assertThat(s.serviceId()).isEqualTo(11);
        assertThat(s.startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(s.endTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(s.price()).isEqualByComparingTo("150.00");
    }

    @Test
    public void rejectsEndNotAfterStart() {
        assertThatThrownBy(() -> BookingSegment.of(11, LocalTime.of(10, 0), LocalTime.of(10, 0), BigDecimal.TEN))
                .isInstanceOf(DomainObjectValidationException.class);
    }

    @Test
    public void rejectsNegativePriceAndNulls() {
        assertThatThrownBy(() -> BookingSegment.of(11, LocalTime.of(9, 0), LocalTime.of(10, 0), new BigDecimal("-1")))
                .isInstanceOf(DomainObjectValidationException.class);
        assertThatThrownBy(() -> BookingSegment.of(null, LocalTime.of(9, 0), LocalTime.of(10, 0), BigDecimal.TEN))
                .isInstanceOf(DomainObjectValidationException.class);
        assertThatThrownBy(() -> BookingSegment.of(11, LocalTime.of(9, 0), LocalTime.of(10, 0), null))
                .isInstanceOf(DomainObjectValidationException.class);
    }
}
