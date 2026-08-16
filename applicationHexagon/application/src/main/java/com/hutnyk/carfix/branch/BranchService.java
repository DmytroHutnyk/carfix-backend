package com.hutnyk.carfix.branch;

import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.branch.exception.InvalidReviewsSortException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.branch.BranchPortIn;
import com.hutnyk.carfix.in.branch.OwnerBranchPortIn;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@ApplicationService
public class BranchService implements BranchPortIn, OwnerBranchPortIn {

    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> REVIEW_SORTS = Set.of(
            BranchReviewsQuery.SORT_NEWEST,
            BranchReviewsQuery.SORT_HIGHEST,
            BranchReviewsQuery.SORT_LOWEST);

    private final BranchPortOut branchPortOut;
    private final ReviewPortOut reviewPortOut;
    private final OwnerBranchPortOut ownerBranchPortOut;
    private final UserPortOut userPortOut;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public BranchView getBranch(UUID branchId) {
        return branchPortOut.findViewById(BranchId.of(branchId))
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    @Override
    @Transactional(readOnly = true)
    public BranchReviewsPage getReviews(BranchReviewsQuery query) {
        String sort = normalizeSort(query.sort());
        if (!branchPortOut.existsActiveById(BranchId.of(query.branchId()))) {
            throw new BranchNotFoundException(query.branchId());
        }
        int size = Math.min(query.size(), MAX_PAGE_SIZE);
        return reviewPortOut.findReviewsPage(
                new BranchReviewsQuery(query.branchId(), sort, query.page(), size));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerBranchSummaryView> getMyBranchSummaries(String ownerEmail) {
        User owner = userPortOut.loadUserByEmail(ownerEmail)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(ownerEmail));
        return ownerBranchPortOut.findSummariesByOwnerId(owner.getId(), clock.instant());
    }

    private static String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return BranchReviewsQuery.SORT_NEWEST;
        }
        String normalized = sort.trim().toLowerCase(Locale.ROOT);
        if (!REVIEW_SORTS.contains(normalized)) {
            throw new InvalidReviewsSortException(sort);
        }
        return normalized;
    }
}
