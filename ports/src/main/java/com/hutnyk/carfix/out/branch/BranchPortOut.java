package com.hutnyk.carfix.out.branch;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.review.BranchRating;

import java.util.Optional;

public interface BranchPortOut {

    /**
     * Writes the cached rating aggregate onto the branch.
     *
     * A targeted projection write rather than update(Branch): these two columns are a cache
     * over `reviews`, not state the Branch aggregate owns — the domain Branch has no rating
     * field. {@link BranchRating#NONE} clears both columns.
     */
    void updateRating(BranchId branchId, BranchRating rating);

    /**
     * The public branch page: branch + address, active services grouped by category,
     * opening hours, serviced brands. Empty when the branch does not exist or is not ACTIVE.
     */
    Optional<BranchView> findViewById(BranchId branchId);

    boolean existsActiveById(BranchId branchId);
}
