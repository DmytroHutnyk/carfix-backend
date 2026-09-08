package com.hutnyk.carfix.out.branch;

import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.review.BranchRating;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BranchPortOut {

    // Rating is a review cache, not Branch state. NONE clears both cached columns.
    void updateRating(BranchId branchId, BranchRating rating);

    Optional<BranchView> findViewById(BranchId branchId);

    boolean existsActiveById(BranchId branchId);

    boolean existsByIdAndOwnerId(BranchId branchId, java.util.UUID ownerId);

    // Branch-local time governs every "now" and "today" decision.
    // Empty also means the branch is missing or inactive.
    Optional<ZoneId> findActiveBranchZone(BranchId branchId);

    Branch insert(Branch branch);

    void insertOpeningHours(List<OpeningHours> openingHours);

    void linkCarBrands(BranchId branchId, Set<Integer> carBrandIds);

    Branch update(Branch branch);

    void replaceOpeningHours(BranchId branchId, List<OpeningHours> openingHours);

    void replaceOpeningHoursExceptions(BranchId branchId, List<OpeningHoursException> exceptions);

    void replaceCarBrands(BranchId branchId, Set<Integer> carBrandIds);
}
