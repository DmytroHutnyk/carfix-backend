package com.hutnyk.carfix.branch;

import com.hutnyk.carfix.address.Address;
import com.hutnyk.carfix.address.CityResolver;
import com.hutnyk.carfix.address.CountryIso;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.branch.exception.InvalidReviewsSortException;
import com.hutnyk.carfix.carCatalog.CarBrand;
import com.hutnyk.carfix.carCatalog.exception.CarBrandNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.employee.Employee;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentType;
import com.hutnyk.carfix.in.branch.BranchPortIn;
import com.hutnyk.carfix.in.branch.OwnerBranchPortIn;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchAddressCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEmployeeCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEquipmentCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchServiceBayCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchServiceCommand;
import com.hutnyk.carfix.in.branch.commands.UpdateBranchOverviewCommand;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchDetailView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.out.address.AddressPortOut;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.carCatalog.CarCatalogPortOut;
import com.hutnyk.carfix.out.employee.EmployeePortOut;
import com.hutnyk.carfix.out.equipment.EquipmentPortOut;
import com.hutnyk.carfix.out.owner.OwnerPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.out.role.RolePortOut;
import com.hutnyk.carfix.out.service.ServiceCategoryPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.out.serviceBay.ServiceBayPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.owner.Owner;
import com.hutnyk.carfix.role.Role;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceCategory;
import com.hutnyk.carfix.service.exception.ServiceCategoryNotFoundException;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayType;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final OwnerPortOut ownerPortOut;
    private final OwnerBranchPortOut ownerBranchPortOut;
    private final UserPortOut userPortOut;
    private final AddressPortOut addressPortOut;
    private final CarCatalogPortOut carCatalogPortOut;
    private final ServiceBayPortOut serviceBayPortOut;
    private final EquipmentPortOut equipmentPortOut;
    private final RolePortOut rolePortOut;
    private final EmployeePortOut employeePortOut;
    private final ServicePortOut servicePortOut;
    private final ServiceCategoryPortOut serviceCategoryPortOut;
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

    @Override
    @Transactional(readOnly = true)
    public OwnerBranchDetailView getMyBranch(String ownerEmail, UUID branchId) {
        User owner = userPortOut.loadUserByEmail(ownerEmail)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(ownerEmail));
        return ownerBranchPortOut.findDetailByIdAndOwnerId(branchId, owner.getId())
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    @Override
    public Branch registerBranch(String ownerEmail, RegisterBranchCommand cmd) {
        Owner owner = ownerPortOut.findOwnerByUsername(ownerEmail)
                .orElseThrow(() -> AuthenticatedUserMissingException.noOwnerAggregate(ownerEmail));
        BranchRegistrationValidator.validate(cmd);
        requireKnownBrands(cmd.carBrandIds());
        requireKnownCategories(cmd.services());

        RegisterBranchAddressCommand a = cmd.address();
        Integer cityId = new CityResolver(addressPortOut)
                .resolveCityId(a.city(), a.region(), CountryIso.parse(a.countryIso()));
        Address address = addressPortOut.insert(Address.of(null, a.streetName(), a.buildingNumber(), a.flatNumber(),
                a.postalCode(), cityId, a.latitude(), a.longitude(), a.googlePlaceId()));

        BranchId branchId = BranchId.genId();
        Branch branch = branchPortOut.insert(Branch.create(branchId, cmd.name().trim(), cmd.phoneNumber(), cmd.email(),
                cmd.timezone(), address.getId(), owner.getUser().getId()));

        branchPortOut.insertOpeningHours(cmd.openingHours().stream()
                .map(h -> OpeningHours.create(h.dayOfWeek(), h.opensAt(), h.closesAt(), h.mode(), branchId))
                .toList());
        if (!cmd.carBrandIds().isEmpty()) {
            branchPortOut.linkCarBrands(branchId, cmd.carBrandIds());
        }

        Map<String, Integer> bayTypeIds = new HashMap<>();
        for (String name : cmd.serviceBayTypes()) {
            bayTypeIds.put(key(name), serviceBayPortOut.insertType(ServiceBayType.create(name.trim(), branchId)).getId());
        }
        for (RegisterBranchServiceBayCommand bay : cmd.serviceBays()) {
            serviceBayPortOut.insert(ServiceBay.create(bay.name().trim(), bayTypeIds.get(key(bay.type())), branchId));
        }

        Map<String, Integer> equipmentTypeIds = new HashMap<>();
        for (String name : cmd.equipmentTypes()) {
            equipmentTypeIds.put(key(name), equipmentPortOut.insertType(EquipmentType.create(name.trim(), branchId)).getId());
        }
        for (RegisterBranchEquipmentCommand unit : cmd.equipment()) {
            equipmentPortOut.insert(Equipment.create(unit.name().trim(), equipmentTypeIds.get(key(unit.type())), branchId));
        }

        Map<String, Integer> roleIds = new HashMap<>();
        for (String name : cmd.roles()) {
            roleIds.put(key(name), rolePortOut.insert(Role.create(name.trim(), branchId)).getId());
        }
        for (RegisterBranchEmployeeCommand employee : cmd.employees()) {
            employeePortOut.insert(Employee.create(EmployeeId.genId(), employee.firstName().trim(), employee.lastName().trim(),
                    branchId, resolve(roleIds, employee.roles())));
        }

        for (RegisterBranchServiceCommand s : cmd.services()) {
            servicePortOut.insert(Service.of(null, s.name().trim(), s.description(), s.durationMinutes(), s.price(),
                    s.status(), branchId, s.categoryId(),
                    resolve(bayTypeIds, s.bayTypes()),
                    s.employeeRequirements().stream()
                            .map(r -> EmployeeRequirement.of(null, r.name(), resolve(roleIds, r.roles())))
                            .toList(),
                    s.equipmentRequirements().stream()
                            .map(r -> EquipmentRequirement.of(null, r.name(), resolve(equipmentTypeIds, r.types())))
                            .toList()));
        }
        return branch;
    }

    @Override
    public OwnerBranchDetailView updateBranchOverview(String ownerEmail, UUID branchId, UpdateBranchOverviewCommand cmd) {
        User owner = userPortOut.loadUserByEmail(ownerEmail)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(ownerEmail));
        OwnerBranchDetailView existing = ownerBranchPortOut.findDetailByIdAndOwnerId(branchId, owner.getId())
                .orElseThrow(() -> new BranchNotFoundException(branchId));

        BranchRegistrationValidator.requireUniqueWeekdays(cmd.openingHours());
        requireKnownBrands(cmd.carBrandIds());

        RegisterBranchAddressCommand a = cmd.address();
        Integer cityId = new CityResolver(addressPortOut)
                .resolveCityId(a.city(), a.region(), CountryIso.parse(a.countryIso()));
        addressPortOut.update(Address.of(existing.address().id(), a.streetName(), a.buildingNumber(), a.flatNumber(),
                a.postalCode(), cityId, a.latitude(), a.longitude(), a.googlePlaceId()));

        BranchId id = BranchId.of(branchId);
        branchPortOut.update(Branch.of(id, cmd.name().trim(), existing.phoneNumber(), existing.email(),
                existing.status(), ZoneId.of(existing.timezone()), existing.address().id(), owner.getId(),
                normalizeDescription(cmd.description()), cmd.cancellationPolicy()));

        branchPortOut.replaceOpeningHours(id, cmd.openingHours().stream()
                .map(h -> OpeningHours.create(h.dayOfWeek(), h.opensAt(), h.closesAt(), h.mode(), id))
                .toList());
        branchPortOut.replaceOpeningHoursExceptions(id, cmd.openingHoursExceptions().stream()
                .map(e -> OpeningHoursException.of(null, e.date(), e.opensAt(), e.closesAt(), e.isOpen(),
                        normalizeDescription(e.reason()), id))
                .toList());
        branchPortOut.replaceCarBrands(id, cmd.carBrandIds());

        return ownerBranchPortOut.findDetailByIdAndOwnerId(branchId, owner.getId())
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    private static String normalizeDescription(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }

    private void requireKnownBrands(Set<Integer> carBrandIds) {
        if (carBrandIds.isEmpty()) {
            return;
        }
        Set<Integer> known = carCatalogPortOut.findAllBrands().stream().map(CarBrand::getId).collect(Collectors.toSet());
        for (Integer id : carBrandIds) {
            if (!known.contains(id)) {
                throw new CarBrandNotFoundException(id);
            }
        }
    }

    private void requireKnownCategories(List<RegisterBranchServiceCommand> services) {
        if (services.isEmpty()) {
            return;
        }
        Set<Integer> known = serviceCategoryPortOut.findAll().stream().map(ServiceCategory::getId).collect(Collectors.toSet());
        for (RegisterBranchServiceCommand service : services) {
            if (!known.contains(service.categoryId())) {
                throw new ServiceCategoryNotFoundException(service.categoryId());
            }
        }
    }

    private static Set<Integer> resolve(Map<String, Integer> ids, List<String> names) {
        return names.stream().map(name -> ids.get(key(name))).collect(Collectors.toSet());
    }

    private static String key(String name) {
        return BranchRegistrationValidator.key(name);
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
