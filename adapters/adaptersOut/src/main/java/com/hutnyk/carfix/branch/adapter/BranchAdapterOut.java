package com.hutnyk.carfix.branch.adapter;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.branch.mapper.BranchMapper;
import com.hutnyk.carfix.branch.repository.BranchRepository;
import com.hutnyk.carfix.carCatalog.repository.CarBrandRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.openingHours.mapper.OpeningHoursMapper;
import com.hutnyk.carfix.openingHours.repository.OpeningHoursExceptionRepository;
import com.hutnyk.carfix.openingHours.repository.OpeningHoursRepository;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.review.BranchRating;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.service.repository.ServiceRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@PersistenceAdapter
public class BranchAdapterOut implements BranchPortOut {

    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final OpeningHoursExceptionRepository openingHoursExceptionRepository;
    private final CarBrandRepository carBrandRepository;
    private final EntityManager entityManager;

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

    @Override
    public Optional<ZoneId> findActiveBranchZone(BranchId branchId) {
        return branchRepository.findTzByIdAndStatus(branchId.id(), BranchStatus.ACTIVE)
                .map(ZoneId::of);
    }

    @Override
    public Branch insert(Branch branch) {
        AddressEntity address = entityManager.getReference(AddressEntity.class, branch.getAddressId());
        BranchEntity entity = BranchMapper.toEntity(branch, address);
        /* app-minted UUID id: persist, not save — save() would merge and pay an extra SELECT */
        entityManager.persist(entity);
        return BranchMapper.toDomain(entity);
    }

    @Override
    public void insertOpeningHours(List<OpeningHours> openingHours) {
        openingHoursRepository.saveAll(openingHours.stream()
                .map(hours -> BranchMapper.toEntity(
                        hours, entityManager.getReference(BranchEntity.class, hours.getBranchId().id())))
                .toList());
    }

    @Override
    public void linkCarBrands(BranchId branchId, Set<Integer> carBrandIds) {
        for (Integer brandId : carBrandIds) {
            carBrandRepository.linkToBranch(brandId, branchId.id());
        }
    }

    @Override
    public Branch update(Branch branch) {
        BranchEntity entity = branchRepository.findById(branch.getId().id())
                .orElseThrow(IllegalStateException::new);
        entity.setName(branch.getName());
        entity.setDescription(branch.getDescription());
        entity.setCancellationPolicy(branch.getCancellationPolicy());
        return BranchMapper.toDomain(entity);
    }

    @Override
    public void replaceOpeningHours(BranchId branchId, List<OpeningHours> openingHours) {
        openingHoursRepository.deleteAllByBranchEntityId(branchId.id());
        insertOpeningHours(openingHours);
    }

    @Override
    public void replaceOpeningHoursExceptions(BranchId branchId, List<OpeningHoursException> exceptions) {
        openingHoursExceptionRepository.deleteAllByBranchEntityId(branchId.id());
        BranchEntity branch = entityManager.getReference(BranchEntity.class, branchId.id());
        openingHoursExceptionRepository.saveAll(exceptions.stream()
                .map(exception -> OpeningHoursMapper.toEntity(exception, branch))
                .toList());
    }

    @Override
    public void replaceCarBrands(BranchId branchId, Set<Integer> carBrandIds) {
        carBrandRepository.unlinkAllFromBranch(branchId.id());
        linkCarBrands(branchId, carBrandIds);
    }
}
