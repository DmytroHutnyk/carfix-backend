package com.hutnyk.carfix.out.branch;

import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.user.UserId;

import java.time.Instant;
import java.util.List;

public interface OwnerBranchPortOut {

    List<OwnerBranchSummaryView> findSummariesByOwnerId(UserId ownerId, Instant now);
}
