package com.example.springbestpractice.api.user;

import com.example.springbestpractice.application.user.AdminUserService;
import com.example.springbestpractice.application.user.command.AdminUserDeleteCommand;
import com.example.springbestpractice.application.user.dto.AdminUserPageRequest;
import com.example.springbestpractice.application.user.dto.AdminUserResponse;
import com.example.springbestpractice.application.user.dto.AdminUserSearchCondition;
import com.example.springbestpractice.application.user.query.AdminUserSearchQuery;
import com.example.springbestpractice.common.annotation.CurrentUser;
import com.example.springbestpractice.common.dto.PageResponse;
import com.example.springbestpractice.common.model.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public PageResponse<AdminUserResponse> getUsers(
            @Valid @ModelAttribute AdminUserPageRequest pageRequest,
            @Valid @ModelAttribute AdminUserSearchCondition condition
    ) {
        return adminUserService.getUsers(AdminUserSearchQuery.from(condition, pageRequest));
    }

    @GetMapping("/{id}")
    public AdminUserResponse getUser(@PathVariable Long id) {
        return adminUserService.getUser(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        adminUserService.deleteUser(AdminUserDeleteCommand.from(id, loginUser));
    }
}
