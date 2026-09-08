package com.hutnyk.carfix.review;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Domain-owned definition of the rating cached on branches: review mean plus review count. */
public record BranchRating(
        //Nullable — null exactly when count is 0
        BigDecimal average,
        int count
) {
    private static final int SCALE = 1;

    // Unrated differs from a zero rating.
    public static final BranchRating NONE = new BranchRating(null, 0);

    public static BranchRating of(List<Integer> starsNumbers) {
        if (starsNumbers == null || starsNumbers.isEmpty()) {
            return NONE;
        }
        int total = starsNumbers.stream().mapToInt(Integer::intValue).sum();
        BigDecimal average = BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(starsNumbers.size()), SCALE, RoundingMode.HALF_UP);
        return new BranchRating(average, starsNumbers.size());
    }

    public boolean isUnrated() {
        return count == 0;
    }
}
