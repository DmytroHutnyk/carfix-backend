package com.hutnyk.carfix.scheduling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RequirementMatcher {

    private RequirementMatcher() {
    }

    public static <R> Optional<Map<Integer, R>> match(List<List<R>> candidatesPerSlot) {
        Map<R, Integer> holderOf = new HashMap<>();
        for (int slot = 0; slot < candidatesPerSlot.size(); slot++) {
            if (!seat(slot, candidatesPerSlot, holderOf, new HashSet<>())) {
                return Optional.empty();
            }
        }
        Map<Integer, R> assignment = new HashMap<>();
        holderOf.forEach((resource, slot) -> assignment.put(slot, resource));
        return Optional.of(Map.copyOf(assignment));
    }

    private static <R> boolean seat(int slot, List<List<R>> candidates,
                                    Map<R, Integer> holderOf, Set<R> tried) {
        for (R resource : candidates.get(slot)) {
            if (!tried.add(resource)) {
                continue;
            }
            Integer holder = holderOf.get(resource);
            if (holder == null || seat(holder, candidates, holderOf, tried)) {
                holderOf.put(resource, slot);
                return true;
            }
        }
        return false;
    }
}
