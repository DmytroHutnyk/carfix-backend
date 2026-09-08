package com.hutnyk.carfix.role.adapter;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.role.RolePortOut;
import com.hutnyk.carfix.role.Role;
import com.hutnyk.carfix.role.mapper.RoleMapper;
import com.hutnyk.carfix.role.repository.RoleRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@PersistenceAdapter
public class RoleAdapterOut implements RolePortOut {

    private final RoleRepository roleRepository;
    private final EntityManager entityManager;

    @Override
    public Role insert(Role role) {
        BranchEntity branch = role.getBranchId() == null
                ? null
                : entityManager.getReference(BranchEntity.class, role.getBranchId().id());
        return RoleMapper.toDomain(roleRepository.save(RoleMapper.toEntity(role, branch)));
    }

    @Override
    public List<Role> findAllForBranch(UUID branchId) {
        return roleRepository.findAllForBranch(branchId).stream().map(RoleMapper::toDomain).toList();
    }
}
