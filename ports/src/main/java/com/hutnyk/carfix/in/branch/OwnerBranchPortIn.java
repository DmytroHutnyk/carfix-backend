package com.hutnyk.carfix.in.branch;

import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;

import java.util.List;

public interface OwnerBranchPortIn {

    List<OwnerBranchSummaryView> getMyBranchSummaries(String ownerEmail);

    /** Creates the whole workshop in one transaction for the owner identified by {@code ownerEmail}. */
    Branch registerBranch(String ownerEmail, RegisterBranchCommand command);
}
