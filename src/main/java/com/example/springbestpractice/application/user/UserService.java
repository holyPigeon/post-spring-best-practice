package com.example.springbestpractice.application.user;

import com.example.springbestpractice.application.user.dto.UserCreateRequest;
import com.example.springbestpractice.application.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.application.user.dto.UserResponse;
import com.example.springbestpractice.application.user.dto.UserUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.DuplicateEmailException;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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

    public UserResponse getUser(Long id, LoginUser loginUser) {
        validateSelf(id, loginUser);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserResponse.from(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request, LoginUser loginUser) {
        validateSelf(id, loginUser);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.updateNickname(request.nickname());
        return UserResponse.from(user);
    }

    @Transactional
    public void updatePassword(Long id, UserPasswordUpdateRequest request, LoginUser loginUser) {
        validateSelf(id, loginUser);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.updatePassword(passwordEncoder.encode(request.password()));
    }

    @Transactional
    public void deleteUser(Long id, LoginUser loginUser) {
        validateSelf(id, loginUser);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }

    private void validateSelf(Long id, LoginUser loginUser) {
        if (loginUser == null || !Objects.equals(id, loginUser.id())) {
            throw new AccessDeniedException("본인 리소스만 접근할 수 있습니다.");
        }
    }
}
