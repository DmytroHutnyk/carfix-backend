package com.hutnyk.carfix.openingHours.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursExceptionEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class OpeningHoursMapperTest {

    private static final UUID BRANCH_ID = UUID.randomUUID();

    private static BranchEntity branch() {
        BranchEntity branch = new BranchEntity();
        branch.setId(BRANCH_ID);
        return branch;
    }

    @Test
    void test_weekly_row_maps_to_domain() {
        //given
        OpeningHoursEntity entity = new OpeningHoursEntity();
        entity.setId(7);
        entity.setDayOfWeek(DayOfWeek.FRIDAY);
        entity.setStartTime(LocalTime.of(8, 0));
        entity.setCloseTime(LocalTime.of(18, 0));
        entity.setBranchEntity(branch());
        //when
        OpeningHours domain = OpeningHoursMapper.toDomain(entity);
        //then
        assertThat(domain.getId()).isEqualTo(7);
        assertThat(domain.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(domain.getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(domain.getCloseTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(domain.getBranchId().id()).isEqualTo(BRANCH_ID);
    }

    @Test
    void test_closed_exception_maps_with_null_hours() {
        //given
        OpeningHoursExceptionEntity entity = new OpeningHoursExceptionEntity();
        entity.setId(3);
        entity.setDate(LocalDate.of(2026, 8, 14));
        entity.setIsOpen(false);
        entity.setReason("holiday");
        entity.setBranchEntity(branch());
        //when
        OpeningHoursException domain = OpeningHoursMapper.toDomain(entity);
        //then
        assertThat(domain.getId()).isEqualTo(3);
        assertThat(domain.getDate()).isEqualTo(LocalDate.of(2026, 8, 14));
        assertThat(domain.getIsOpen()).isFalse();
        assertThat(domain.getStartTime()).isNull();
        assertThat(domain.getCloseTime()).isNull();
        assertThat(domain.getReason()).isEqualTo("holiday");
        assertThat(domain.getBranchId().id()).isEqualTo(BRANCH_ID);
    }

    @Test
    void test_open_exception_maps_hours() {
        //given
        OpeningHoursExceptionEntity entity = new OpeningHoursExceptionEntity();
        entity.setId(4);
        entity.setDate(LocalDate.of(2026, 8, 15));
        entity.setIsOpen(true);
        entity.setStartTime(LocalTime.of(9, 0));
        entity.setCloseTime(LocalTime.of(13, 0));
        entity.setBranchEntity(branch());
        //when
        OpeningHoursException domain = OpeningHoursMapper.toDomain(entity);
        //then
        assertThat(domain.getIsOpen()).isTrue();
        assertThat(domain.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(domain.getCloseTime()).isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    void test_null_entities_map_to_null() {
        assertThat(OpeningHoursMapper.toDomain((OpeningHoursEntity) null)).isNull();
        assertThat(OpeningHoursMapper.toDomain((OpeningHoursExceptionEntity) null)).isNull();
    }
}
