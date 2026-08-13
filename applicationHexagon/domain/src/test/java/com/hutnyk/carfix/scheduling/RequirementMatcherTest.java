package com.hutnyk.carfix.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class RequirementMatcherTest {

    @Test
    void test_greedy_trap_slot_A_only_jan_slot_B_jan_or_anna() {
        Optional<Map<Integer, String>> match = RequirementMatcher.match(List.of(
                List.of("jan", "anna"),
                List.of("jan")));
        assertThat(match).isPresent();
        assertThat(match.get()).containsEntry(0, "anna").containsEntry(1, "jan");
    }

    @Test
    void test_more_slots_than_distinct_resources_fails() {
        assertThat(RequirementMatcher.match(List.of(
                List.of("jan", "anna"),
                List.of("jan", "anna"),
                List.of("jan", "anna")))).isEmpty();
    }

    @Test
    void test_one_resource_qualifying_for_two_slots_fills_only_one() {
        assertThat(RequirementMatcher.match(List.of(
                List.of("jan"),
                List.of("jan")))).isEmpty();
    }

    @Test
    void test_slot_with_zero_candidates_fails() {
        assertThat(RequirementMatcher.match(List.of(
                List.of("jan"),
                List.<String>of()))).isEmpty();
    }

    @Test
    void test_zero_slots_succeeds_with_empty_assignment() {
        assertThat(RequirementMatcher.match(List.<List<String>>of()))
                .contains(Map.of());
    }

    @Test
    void test_assignment_uses_only_declared_candidates_and_is_distinct() {
        Optional<Map<Integer, String>> match = RequirementMatcher.match(List.of(
                List.of("a", "b"),
                List.of("b", "c"),
                List.of("c", "a")));
        assertThat(match).isPresent();
        Map<Integer, String> assignment = match.get();
        assertThat(assignment.get(0)).isIn("a", "b");
        assertThat(assignment.get(1)).isIn("b", "c");
        assertThat(assignment.get(2)).isIn("c", "a");
        assertThat(assignment.values()).doesNotHaveDuplicates();
    }

    @Test
    void test_deterministic_same_input_same_assignment() {
        List<List<String>> input = List.of(List.of("a", "b"), List.of("a", "c"));
        assertThat(RequirementMatcher.match(input)).isEqualTo(RequirementMatcher.match(input));
    }
}
