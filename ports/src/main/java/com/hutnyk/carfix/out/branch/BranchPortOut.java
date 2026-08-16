package com.hutnyk.carfix.out.branch;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.review.BranchRating;

import java.time.ZoneId;
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

    /**
     * The branch's local time zone, which is the authority for every "now"/"today" decision
     * about that branch. Empty when the branch does not exist or is not ACTIVE, so one call
     * answers existence and zone together.
     */
    Optional<ZoneId> findActiveBranchZone(BranchId branchId);
}
