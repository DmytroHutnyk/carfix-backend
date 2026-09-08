package com.hutnyk.carfix.openingHours;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

public class OpeningHoursTest {

    private static final BranchId BRANCH_ID = BranchId.genId();

    @Test
    public void test_create_keeps_mode_and_has_no_id() {
        OpeningHours hours = OpeningHours.create(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(14, 0),
                OpeningHoursMode.BY_APPOINTMENT, BRANCH_ID);

        assertThat(hours.getId()).isNull();
        assertThat(hours.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(hours.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(hours.getCloseTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(hours.getMode()).isEqualTo(OpeningHoursMode.BY_APPOINTMENT);
        assertThat(hours.getBranchId()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_create_rejects_close_not_after_open() {
        assertThatThrownBy(() -> OpeningHours.create(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 0),
                OpeningHoursMode.OPEN, BRANCH_ID))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.INVALID_TIME_RANGE);
    }

    @Test
    public void test_of_requires_mode() {
        assertThatThrownBy(() -> OpeningHours.of(1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), null, BRANCH_ID))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("fieldName")
                .isEqualTo("mode");
    }

    @Test
    public void test_mode_constants_match_db_check() {
        assertThat(OpeningHoursMode.values()).extracting(Enum::name).containsExactly("OPEN", "BY_APPOINTMENT");
    }
}
