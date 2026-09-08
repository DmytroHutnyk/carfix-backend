package com.hutnyk.carfix.in.branch;

import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.in.branch.commands.UpdateBranchOverviewCommand;
import com.hutnyk.carfix.in.branch.query.OwnerBranchDetailView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;

import java.util.List;
import java.util.UUID;

public interface OwnerBranchPortIn {

    List<OwnerBranchSummaryView> getMyBranchSummaries(String ownerEmail);

    OwnerBranchDetailView getMyBranch(String ownerEmail, UUID branchId);

    Branch registerBranch(String ownerEmail, RegisterBranchCommand command);

    OwnerBranchDetailView updateBranchOverview(String ownerEmail, UUID branchId, UpdateBranchOverviewCommand command);
}
