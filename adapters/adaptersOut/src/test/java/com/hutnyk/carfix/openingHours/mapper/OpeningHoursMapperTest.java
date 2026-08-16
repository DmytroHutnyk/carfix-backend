package com.hutnyk.carfix.openingHours.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursExceptionEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class OpeningHoursMapperTest {

    private static final UUID BRANCH_ID = UUID.randomUUID();

    private static BranchEntity branch() {
        BranchEntity branch = new BranchEntity();
        branch.setId(BRANCH_ID);
        return branch;
    }

    @Test
    public void test_toDomain_maps_regular_hours() {
        //given
        OpeningHoursEntity entity = new OpeningHoursEntity();
        entity.setId(7);
        entity.setDayOfWeek(DayOfWeek.MONDAY);
        entity.setStartTime(LocalTime.of(8, 0));
        entity.setCloseTime(LocalTime.of(18, 0));
        entity.setBranchEntity(branch());

        //when
        OpeningHours hours = OpeningHoursMapper.toDomain(entity);

        //then
        assertThat(hours.getId()).isEqualTo(7);
        assertThat(hours.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(hours.getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(hours.getCloseTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(hours.getBranchId().id()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_toDomain_maps_closed_exception_without_hours() {
        //given
        OpeningHoursExceptionEntity entity = new OpeningHoursExceptionEntity();
        entity.setId(3);
        entity.setDate(LocalDate.of(2026, 12, 25));
        entity.setIsOpen(false);
        entity.setReason("Christmas");
        entity.setBranchEntity(branch());

        //when
        OpeningHoursException exception = OpeningHoursMapper.toDomain(entity);

        //then
        assertThat(exception.getDate()).isEqualTo(LocalDate.of(2026, 12, 25));
        assertThat(exception.getIsOpen()).isFalse();
        assertThat(exception.getStartTime()).isNull();
        assertThat(exception.getReason()).isEqualTo("Christmas");
        assertThat(exception.getBranchId().id()).isEqualTo(BRANCH_ID);
    }

    @Test
    public void test_toDomain_null_guards() {
        assertThat(OpeningHoursMapper.toDomain((OpeningHoursEntity) null)).isNull();
        assertThat(OpeningHoursMapper.toDomain((OpeningHoursExceptionEntity) null)).isNull();
    }
}
