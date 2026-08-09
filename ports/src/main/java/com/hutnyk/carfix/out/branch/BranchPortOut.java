package com.hutnyk.carfix.out.branch;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.review.BranchRating;

public interface BranchPortOut {

    /**
     * Writes the cached rating aggregate onto the branch.
     *
     * A targeted projection write rather than update(Branch): these two columns are a cache
     * over `reviews`, not state the Branch aggregate owns — the domain Branch has no rating
     * field. {@link BranchRating#NONE} clears both columns.
     */
    void updateRating(BranchId branchId, BranchRating rating);
}
