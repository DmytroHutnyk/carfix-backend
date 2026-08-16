package com.hutnyk.carfix.in.branch;

import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;

import java.util.List;

public interface OwnerBranchPortIn {

    List<OwnerBranchSummaryView> getMyBranchSummaries(String ownerEmail);
}
