package com.example.springbestpractice.user.service;

import com.example.springbestpractice.user.dto.UserCreateRequest;
import com.example.springbestpractice.user.dto.UserResponse;
import com.example.springbestpractice.user.dto.UserUpdateRequest;
import com.example.springbestpractice.user.entity.User;
import com.example.springbestpractice.user.exception.DuplicateEmailException;
import com.example.springbestpractice.user.exception.UserNotFoundException;
import com.example.springbestpractice.user.repository.UserRepository;
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
        User user = User.create(request.email(), request.nickname());
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
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }
}