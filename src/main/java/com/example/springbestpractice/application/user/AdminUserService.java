package com.example.springbestpractice.application.user;

import com.example.springbestpractice.application.user.dto.AdminUserResponse;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public List<AdminUserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    public AdminUserResponse getUser(Long id) {
        User user = getUserById(id);
        return AdminUserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
