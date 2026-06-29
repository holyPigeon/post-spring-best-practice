package com.example.springbestpractice.application.user;

import com.example.springbestpractice.application.user.command.AdminUserDeleteCommand;
import com.example.springbestpractice.application.user.dto.AdminUserPageRequest;
import com.example.springbestpractice.application.user.dto.AdminUserResponse;
import com.example.springbestpractice.common.dto.PageResponse;
import com.example.springbestpractice.common.exception.ConflictException;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.domain.user.UserRole;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final UserRepository userRepository;

    public PageResponse<AdminUserResponse> getUsers(AdminUserPageRequest request) {
        PageRequest pageable = PageRequest.of(request.page(), request.size(), DEFAULT_SORT);
        return PageResponse.from(userRepository.findAll(pageable).map(AdminUserResponse::from));
    }

    public AdminUserResponse getUser(Long id) {
        User user = getUserById(id);
        return AdminUserResponse.from(user);
    }

    @Transactional
    public void deleteUser(AdminUserDeleteCommand command) {
        User user = getUserById(command.targetUserId());
        validateDeletable(user, command.loginUser());
        userRepository.delete(user);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void validateDeletable(User user, LoginUser loginUser) {
        if (user.getId().equals(loginUser.id())) {
            throw new ConflictException("자기 자신은 삭제할 수 없습니다.");
        }
        if (user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1) {
            throw new ConflictException("마지막 관리자는 삭제할 수 없습니다.");
        }
    }
}
