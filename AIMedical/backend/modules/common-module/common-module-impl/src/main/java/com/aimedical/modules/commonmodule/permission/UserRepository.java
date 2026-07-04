package com.aimedical.modules.commonmodule.permission;

import com.aimedical.modules.commonmodule.api.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByPhone(String phone);

    @EntityGraph(attributePaths = {"roles", "posts", "posts.functions"})
    Optional<User> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"roles", "posts", "posts.functions"})
    Optional<User> findWithDetailsForMenuById(Long id);

    @Query("SELECT u.tokenVersion FROM User u WHERE u.id = :id")
    Optional<Integer> findTokenVersionById(@Param("id") Long id);

    /**
     * 管理员端用户分页查询：根据用户名/昵称/手机号关键字 + 用户类型筛选。
     * 关键字为空时按 userType 过滤；userType 为空时按关键字过滤；都为空返回全部。
     */
    @Query("""
            SELECT u FROM User u
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:userType IS NULL OR u.userType = :userType)
            """)
    Page<User> findByKeywordAndUserType(@Param("keyword") String keyword,
                                        @Param("userType") UserType userType,
                                        Pageable pageable);
}
