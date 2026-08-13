package com.hutnyk.carfix.in.branch;

import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.in.branch.query.BranchView;

import java.util.UUID;

public interface BranchPortIn {

    BranchView getBranch(UUID branchId);

    BranchReviewsPage getReviews(BranchReviewsQuery query);
}
