package com.hutnyk.carfix.branch.adapter;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.branch.repository.BranchRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.review.BranchRating;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PersistenceAdapter
public class BranchAdapterOut implements BranchPortOut {

    private final BranchRepository branchRepository;

    @Override
    public void updateRating(BranchId branchId, BranchRating rating) {
        /* The service has already established the branch exists (it came from a booking),
           so absence here is a programming error, not a 404. */
        BranchEntity branch = branchRepository.findById(branchId.id())
                .orElseThrow(IllegalStateException::new);
        /* check_branches_rating_pair: both columns null, or neither. */
        branch.setRating(rating.isUnrated() ? null : rating.average());
        branch.setReviewCount(rating.isUnrated() ? null : rating.count());
    }
}
