package com.aimedical.modules.commonmodule.dto.response;

import com.aimedical.modules.commonmodule.api.UserType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户响应 DTO。
 *
 * <p>不返回 password 字段。角色和岗位返回简化的 ID+名称集合。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class UserResponse {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Boolean enabled;
    private Boolean passwordChangeRequired;
    private Integer tokenVersion;
    private UserType userType;
    private String gender;
    private Integer age;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 关联角色概要集合 */
    private Set<RoleBrief> roles;

    /** 关联岗位概要集合 */
    private Set<PostBrief> posts;

    /**
     * 角色简表（避免序列化双向关联导致无限递归）。
     */
    @Data
    public static class RoleBrief {
        private Long id;
        private String code;
        private String name;

        public RoleBrief(Long id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 岗位简表。
     */
    @Data
    public static class PostBrief {
        private Long id;
        private String code;
        private String name;

        public PostBrief(Long id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }
    }
}
