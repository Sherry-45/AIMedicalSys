package com.aimedical.modules.commonmodule.controller;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.dto.response.PostResponse;
import com.aimedical.modules.commonmodule.permission.Post;
import com.aimedical.modules.commonmodule.permission.PostRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 岗位管理控制器（管理员端）。
 *
 * <p>当前仅暴露"查询所有启用岗位"接口，用于用户管理表单中的岗位下拉选择。
 * 完整的岗位 CRUD 后续可按需扩展。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/admin/posts")
@PreAuthorize("hasRole('ADMIN')")
public class PostManagementController {

    private final PostRepository postRepository;

    public PostManagementController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/all-enabled")
    public Result<List<PostResponse>> listAllEnabled() {
        List<PostResponse> list = postRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getEnabled()))
                .sorted(Comparator
                        .comparing(Post::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Post::getId))
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    private PostResponse toResponse(Post post) {
        PostResponse resp = new PostResponse();
        resp.setId(post.getId());
        resp.setCode(post.getCode());
        resp.setName(post.getName());
        resp.setDescription(post.getDescription());
        resp.setEnabled(post.getEnabled());
        resp.setSort(post.getSort());
        if (post.getRole() != null) {
            resp.setRoleId(post.getRole().getId());
            resp.setRoleName(post.getRole().getName());
        }
        resp.setRemark(post.getRemark());
        resp.setCreatedAt(post.getCreatedAt());
        resp.setUpdatedAt(post.getUpdatedAt());
        return resp;
    }
}
