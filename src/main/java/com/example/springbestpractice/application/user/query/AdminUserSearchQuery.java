package com.example.springbestpractice.application.user.query;

import com.example.springbestpractice.application.user.dto.AdminUserPageRequest;
import com.example.springbestpractice.application.user.dto.AdminUserSearchCondition;
import com.example.springbestpractice.domain.user.UserRole;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record AdminUserSearchQuery(
        String keyword,
        UserRole role,
        Pageable pageable
) {
    public static AdminUserSearchQuery from(AdminUserSearchCondition condition, AdminUserPageRequest pageRequest) {
        return new AdminUserSearchQuery(
                condition.keyword(),
                condition.role(),
                PageRequest.of(pageRequest.page(), pageRequest.size(), pageRequest.sort().getSort())
        );
    }
}
