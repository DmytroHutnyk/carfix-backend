package com.hutnyk.carfix.in.branch;

import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.in.branch.query.OwnerBranchDetailView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;

import java.util.List;
import java.util.UUID;

public interface OwnerBranchPortIn {

    List<OwnerBranchSummaryView> getMyBranchSummaries(String ownerEmail);

    /** The owner's editable view of one of their own branches; not-found when someone else owns it. */
    OwnerBranchDetailView getMyBranch(String ownerEmail, UUID branchId);

    /** Creates the whole workshop in one transaction for the owner identified by {@code ownerEmail}. */
    Branch registerBranch(String ownerEmail, RegisterBranchCommand command);
}
