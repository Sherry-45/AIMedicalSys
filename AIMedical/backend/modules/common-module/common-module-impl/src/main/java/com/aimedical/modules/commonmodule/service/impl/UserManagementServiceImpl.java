package com.aimedical.modules.commonmodule.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.CommonModuleErrorCode;
import com.aimedical.modules.commonmodule.dto.request.UserCreateRequest;
import com.aimedical.modules.commonmodule.dto.request.UserPasswordResetRequest;
import com.aimedical.modules.commonmodule.dto.request.UserQueryRequest;
import com.aimedical.modules.commonmodule.dto.request.UserUpdateRequest;
import com.aimedical.modules.commonmodule.dto.response.UserResponse;
import com.aimedical.modules.commonmodule.permission.Post;
import com.aimedical.modules.commonmodule.permission.PostRepository;
import com.aimedical.modules.commonmodule.permission.Role;
import com.aimedical.modules.commonmodule.permission.RoleRepository;
import com.aimedical.modules.commonmodule.permission.User;
import com.aimedical.modules.commonmodule.permission.UserRepository;
import com.aimedical.modules.commonmodule.service.UserManagementService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现（管理员端）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementServiceImpl(UserRepository userRepository,
                                     RoleRepository roleRepository,
                                     PostRepository postRepository,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Result<UserResponse> create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return Result.fail(CommonModuleErrorCode.USERNAME_DUPLICATE);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType());
        user.setGender(request.getGender());
        user.setAge(request.getAge());
        user.setEnabled(true);
        user.setPasswordChangeRequired(
                Boolean.TRUE.equals(request.getPasswordChangeRequired()));
        user.setTokenVersion(0);
        user.setRemark(request.getRemark());

        // 关联角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = resolveRoles(request.getRoleIds());
            if (roles.size() != request.getRoleIds().size()) {
                return Result.fail(CommonModuleErrorCode.ROLE_NOT_FOUND);
            }
            user.setRoles(roles);
        }

        // 关联岗位
        if (request.getPostIds() != null && !request.getPostIds().isEmpty()) {
            Set<Post> posts = resolvePosts(request.getPostIds());
            if (posts.size() != request.getPostIds().size()) {
                return Result.fail(CommonModuleErrorCode.POST_NOT_FOUND);
            }
            user.setPosts(posts);
        }

        try {
            User saved = userRepository.save(user);
            // 重新加载以填充关联（roles/posts 是 LAZY）
            return userRepository.findWithDetailsById(saved.getId())
                    .map(this::toResponse)
                    .map(Result::success)
                    .orElseGet(() -> Result.success(toResponse(saved)));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<UserResponse> update(Long id, UserUpdateRequest request) {
        Optional<User> opt = userRepository.findWithDetailsById(id);
        if (opt.isEmpty()) {
            return Result.fail(CommonModuleErrorCode.USER_NOT_FOUND);
        }
        User user = opt.get();

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getPasswordChangeRequired() != null) {
            user.setPasswordChangeRequired(request.getPasswordChangeRequired());
        }
        if (request.getRemark() != null) {
            user.setRemark(request.getRemark());
        }
        if (request.getRoleIds() != null) {
            Set<Role> roles = resolveRoles(request.getRoleIds());
            if (roles.size() != request.getRoleIds().size()) {
                return Result.fail(CommonModuleErrorCode.ROLE_NOT_FOUND);
            }
            user.setRoles(roles);
        }
        if (request.getPostIds() != null) {
            Set<Post> posts = resolvePosts(request.getPostIds());
            if (posts.size() != request.getPostIds().size()) {
                return Result.fail(CommonModuleErrorCode.POST_NOT_FOUND);
            }
            user.setPosts(posts);
        }

        try {
            User saved = userRepository.save(user);
            return userRepository.findWithDetailsById(saved.getId())
                    .map(this::toResponse)
                    .map(Result::success)
                    .orElseGet(() -> Result.success(toResponse(saved)));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(CommonModuleErrorCode.USER_NOT_FOUND);
        }
        // 软删除：BaseEntity 的 @SQLDelete 会将 deleted 置为 true
        userRepository.delete(opt.get());
        return Result.success(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<UserResponse> getById(Long id) {
        return userRepository.findWithDetailsById(id)
                .map(this::toResponse)
                .map(Result::success)
                .orElseGet(() -> Result.fail(CommonModuleErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Page<UserResponse>> query(UserQueryRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        String keyword = (request.getKeyword() == null || request.getKeyword().isBlank())
                ? null : request.getKeyword().trim();
        Page<User> entityPage = userRepository.findByKeywordAndUserType(
                keyword, request.getUserType(), pageable);

        Page<UserResponse> responsePage = entityPage.map(this::toResponse);

        // enabled 过滤在内存中完成（避免组合查询方法爆炸）
        if (request.getEnabled() != null) {
            List<UserResponse> filtered = responsePage.getContent().stream()
                    .filter(r -> request.getEnabled().equals(r.getEnabled()))
                    .toList();
            return Result.success(new org.springframework.data.domain.PageImpl<>(
                    filtered, pageable, filtered.size()));
        }
        return Result.success(responsePage);
    }

    @Override
    @Transactional
    public Result<UserResponse> toggleEnabled(Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(CommonModuleErrorCode.USER_NOT_FOUND);
        }
        User user = opt.get();
        user.setEnabled(!Boolean.TRUE.equals(user.getEnabled()));
        try {
            User saved = userRepository.save(user);
            return userRepository.findWithDetailsById(saved.getId())
                    .map(this::toResponse)
                    .map(Result::success)
                    .orElseGet(() -> Result.success(toResponse(saved)));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<Void> resetPassword(Long id, UserPasswordResetRequest request) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(CommonModuleErrorCode.USER_NOT_FOUND);
        }
        User user = opt.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // 重置密码后递增 tokenVersion，使旧令牌失效
        user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);
        user.setPasswordChangeRequired(
                request.getPasswordChangeRequired() == null
                        ? true : request.getPasswordChangeRequired());
        try {
            userRepository.save(user);
            return Result.success(null);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    // ---- 内部工具方法 ----

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        return roleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toSet());
    }

    private Set<Post> resolvePosts(Set<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new HashSet<>();
        }
        return postRepository.findAllById(postIds).stream()
                .collect(Collectors.toSet());
    }

    private UserResponse toResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setPhone(user.getPhone());
        resp.setEmail(user.getEmail());
        resp.setEnabled(user.getEnabled());
        resp.setPasswordChangeRequired(user.getPasswordChangeRequired());
        resp.setTokenVersion(user.getTokenVersion());
        resp.setUserType(user.getUserType());
        resp.setGender(user.getGender());
        resp.setAge(user.getAge());
        resp.setRemark(user.getRemark());
        resp.setCreatedAt(user.getCreatedAt());
        resp.setUpdatedAt(user.getUpdatedAt());

        Set<UserResponse.RoleBrief> roleBriefs = user.getRoles() == null
                ? Collections.emptySet()
                : user.getRoles().stream()
                        .map(r -> new UserResponse.RoleBrief(r.getId(), r.getCode(), r.getName()))
                        .collect(Collectors.toSet());
        resp.setRoles(roleBriefs);

        Set<UserResponse.PostBrief> postBriefs = user.getPosts() == null
                ? Collections.emptySet()
                : user.getPosts().stream()
                        .map(p -> new UserResponse.PostBrief(p.getId(), p.getCode(), p.getName()))
                        .collect(Collectors.toSet());
        resp.setPosts(postBriefs);

        return resp;
    }
}
