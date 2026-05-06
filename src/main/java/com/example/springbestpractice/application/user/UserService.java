package com.example.springbestpractice.application.user;

import com.example.springbestpractice.api.user.dto.UserCreateRequest;
import com.example.springbestpractice.api.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.api.user.dto.UserResponse;
import com.example.springbestpractice.api.user.dto.UserUpdateRequest;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.DuplicateEmailException;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        User user = User.create(request.email(), request.nickname(), request.password());
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse getUser(Long id) {
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
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.updateNickname(request.nickname());
        return UserResponse.from(user);
    }

    @Transactional
    public void updatePassword(Long id, UserPasswordUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.updatePassword(request.password());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }
}