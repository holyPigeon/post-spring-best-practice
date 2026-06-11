package com.example.springbestpractice.application.user;

import com.example.springbestpractice.application.user.command.UserDeleteCommand;
import com.example.springbestpractice.application.user.command.UserPasswordUpdateCommand;
import com.example.springbestpractice.application.user.command.UserUpdateCommand;
import com.example.springbestpractice.application.user.dto.UserCreateRequest;
import com.example.springbestpractice.application.user.dto.UserResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.user.DuplicateEmailException;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        User user = User.create(request.email(), request.nickname(), passwordEncoder.encode(request.password()));
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse getMyProfile(LoginUser loginUser) {
        return UserResponse.from(getCurrentUser(loginUser));
    }

    @Transactional
    public UserResponse updateMyProfile(UserUpdateCommand command) {
        User user = getCurrentUser(command.loginUser());
        user.updateNickname(command.nickname());
        return UserResponse.from(user);
    }

    @Transactional
    public void updatePassword(UserPasswordUpdateCommand command) {
        User user = getCurrentUser(command.loginUser());
        user.updatePassword(passwordEncoder.encode(command.password()));
    }

    @Transactional
    public void deleteMyAccount(UserDeleteCommand command) {
        User user = getCurrentUser(command.loginUser());
        userRepository.delete(user);
    }

    private User getCurrentUser(LoginUser loginUser) {
        LoginUser currentUser = requireLoginUser(loginUser);
        return userRepository.findById(currentUser.id())
                .orElseThrow(() -> new UserNotFoundException(currentUser.id()));
    }

    private LoginUser requireLoginUser(LoginUser loginUser) {
        if (loginUser == null) {
            throw new AccessDeniedException("인증된 사용자 정보가 필요합니다.");
        }
        return loginUser;
    }
}
