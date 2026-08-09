package com.hutnyk.carfix.review;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * A branch's rating as its customers left it: the mean of every review's stars, and how
 * many there were.
 *
 * This is the definition of the number `branches.rating` caches. It lives here, not in a
 * SQL AVG and not in a service, because "a branch is rated the average of its reviews" is
 * a business fact — the seam where a future weighting (recency, verified visits) lands.
 *
 * @param average one decimal place, or {@code null} when there are no reviews
 * @param count   how many reviews the average is over; 0 when there are none
 */
public record BranchRating(
        //Nullable — null exactly when count is 0
        BigDecimal average,
        int count
) {
    private static final int SCALE = 1;

    /** A branch nobody has reviewed yet. Unrated, which is not the same as rated zero. */
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
