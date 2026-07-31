package com.hutnyk.carfix.error;

import com.hutnyk.carfix.exception.ErrorCategory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorStatusMapperTest {

    @Test
    public void test_every_category_maps_to_a_status_and_a_title() {
        //then — the net that catches "added a category, forgot the mapping" if anyone
        //adds a default branch to the switch and defeats the compiler's exhaustiveness check
        for (ErrorCategory category : ErrorCategory.values()) {
            assertThat(ErrorStatusMapper.statusOf(category)).as(category.name()).isNotNull();
            assertThat(ErrorStatusMapper.titleOf(category)).as(category.name()).isNotBlank();
            assertThat(ErrorStatusMapper.summaryOf(category)).as(category.name()).isNotBlank();
        }
    }

    @Test
    public void test_the_agreed_status_mapping() {
        //then
        assertThat(ErrorStatusMapper.statusOf(ErrorCategory.VALIDATION)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorStatusMapper.statusOf(ErrorCategory.NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorStatusMapper.statusOf(ErrorCategory.CONFLICT)).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorStatusMapper.statusOf(ErrorCategory.AUTHENTICATION)).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorStatusMapper.statusOf(ErrorCategory.AUTHORIZATION)).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ErrorStatusMapper.statusOf(ErrorCategory.BUSINESS_RULE)).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ErrorStatusMapper.statusOf(ErrorCategory.INTEGRATION)).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(ErrorStatusMapper.statusOf(ErrorCategory.INTERNAL)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
