package com.hutnyk.carfix.in.scheduling;

import com.hutnyk.carfix.in.scheduling.query.BranchSlotsQuery;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsView;

public interface SlotPortIn {

    BranchSlotsView getSlots(BranchSlotsQuery query);
}
