package com.hutnyk.carfix.branch.adapter;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.branch.mapper.BranchMapper;
import com.hutnyk.carfix.branch.repository.BranchRepository;
import com.hutnyk.carfix.carCatalog.repository.CarBrandRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.openingHours.repository.OpeningHoursRepository;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.review.BranchRating;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.service.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@PersistenceAdapter
public class BranchAdapterOut implements BranchPortOut {

    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final CarBrandRepository carBrandRepository;

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

    @Override
    public Optional<BranchView> findViewById(BranchId branchId) {
        UUID id = branchId.id();
        return branchRepository.findWithAddressByIdAndStatus(id, BranchStatus.ACTIVE)
                .map(branch -> BranchMapper.toView(
                        branch,
                        serviceRepository.findAllWithCategoryByBranchIdAndStatus(id, ServiceStatus.ACTIVE),
                        openingHoursRepository.findAllByBranchEntityId(id),
                        carBrandRepository.findAllByBranchId(id)));
    }

    @Override
    public boolean existsActiveById(BranchId branchId) {
        return branchRepository.existsByIdAndStatus(branchId.id(), BranchStatus.ACTIVE);
    }
}
