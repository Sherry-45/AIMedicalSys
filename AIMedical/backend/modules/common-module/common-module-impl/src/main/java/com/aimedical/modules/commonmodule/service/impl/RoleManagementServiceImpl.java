package com.aimedical.modules.commonmodule.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.CommonModuleErrorCode;
import com.aimedical.modules.commonmodule.dto.request.RoleCreateRequest;
import com.aimedical.modules.commonmodule.dto.request.RoleQueryRequest;
import com.aimedical.modules.commonmodule.dto.request.RoleUpdateRequest;
import com.aimedical.modules.commonmodule.dto.response.RoleResponse;
import com.aimedical.modules.commonmodule.permission.Role;
import com.aimedical.modules.commonmodule.permission.RoleRepository;
import com.aimedical.modules.commonmodule.service.RoleManagementService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现（管理员端）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class RoleManagementServiceImpl implements RoleManagementService {

    private final RoleRepository roleRepository;

    public RoleManagementServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public Result<RoleResponse> create(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            return Result.fail(CommonModuleErrorCode.ROLE_CODE_DUPLICATE);
        }
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        role.setSort(request.getSort() == null ? 0 : request.getSort());
        role.setRemark(request.getRemark());

        try {
            Role saved = roleRepository.save(role);
            return Result.success(toResponse(saved));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<RoleResponse> update(Long id, RoleUpdateRequest request) {
        Optional<Role> opt = roleRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(CommonModuleErrorCode.ROLE_NOT_FOUND);
        }
        Role role = opt.get();
        if (request.getName() != null) role.setName(request.getName());
        if (request.getDescription() != null) role.setDescription(request.getDescription());
        if (request.getEnabled() != null) role.setEnabled(request.getEnabled());
        if (request.getSort() != null) role.setSort(request.getSort());
        if (request.getRemark() != null) role.setRemark(request.getRemark());

        try {
            Role saved = roleRepository.save(role);
            return Result.success(toResponse(saved));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        Optional<Role> opt = roleRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(CommonModuleErrorCode.ROLE_NOT_FOUND);
        }
        roleRepository.delete(opt.get());
        return Result.success(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<RoleResponse> getById(Long id) {
        return roleRepository.findById(id)
                .map(this::toResponse)
                .map(Result::success)
                .orElseGet(() -> Result.fail(CommonModuleErrorCode.ROLE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Page<RoleResponse>> query(RoleQueryRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "sort").and(Sort.by(Sort.Direction.DESC, "createdAt")));

        Page<Role> entityPage = roleRepository.findAll(pageable);

        String keyword = request.getKeyword() == null ? null : request.getKeyword().trim().toLowerCase();
        Page<RoleResponse> responsePage = entityPage.map(this::toResponse);

        // 关键字 + enabled 过滤在内存中完成
        List<RoleResponse> filtered = responsePage.getContent().stream()
                .filter(r -> keyword == null || keyword.isEmpty()
                        || (r.getCode() != null && r.getCode().toLowerCase().contains(keyword))
                        || (r.getName() != null && r.getName().toLowerCase().contains(keyword)))
                .filter(r -> request.getEnabled() == null || request.getEnabled().equals(r.getEnabled()))
                .toList();

        return Result.success(new PageImpl<>(filtered, pageable, filtered.size()));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<RoleResponse>> listAllEnabled() {
        List<RoleResponse> list = roleRepository.findAll().stream()
                .filter(r -> Boolean.TRUE.equals(r.getEnabled()))
                .sorted(Comparator.comparing(Role::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Role::getId))
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    private RoleResponse toResponse(Role role) {
        RoleResponse resp = new RoleResponse();
        resp.setId(role.getId());
        resp.setCode(role.getCode());
        resp.setName(role.getName());
        resp.setDescription(role.getDescription());
        resp.setEnabled(role.getEnabled());
        resp.setSort(role.getSort());
        resp.setRemark(role.getRemark());
        resp.setCreatedAt(role.getCreatedAt());
        resp.setUpdatedAt(role.getUpdatedAt());
        return resp;
    }
}
