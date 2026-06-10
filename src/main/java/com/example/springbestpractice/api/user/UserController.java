package com.example.springbestpractice.api.user;

import com.example.springbestpractice.application.user.UserService;
import com.example.springbestpractice.application.user.command.UserDeleteCommand;
import com.example.springbestpractice.application.user.command.UserPasswordUpdateCommand;
import com.example.springbestpractice.application.user.command.UserUpdateCommand;
import com.example.springbestpractice.application.user.dto.UserCreateRequest;
import com.example.springbestpractice.application.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.application.user.dto.UserResponse;
import com.example.springbestpractice.application.user.dto.UserUpdateRequest;
import com.example.springbestpractice.common.annotation.CurrentUser;
import com.example.springbestpractice.common.model.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        return userService.getUser(id, loginUser);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @CurrentUser LoginUser loginUser
    ) {
        return userService.updateUser(UserUpdateCommand.from(id, request, loginUser));
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserPasswordUpdateRequest request,
            @CurrentUser LoginUser loginUser
    ) {
        userService.updatePassword(UserPasswordUpdateCommand.from(id, request, loginUser));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        userService.deleteUser(UserDeleteCommand.from(id, loginUser));
    }
}
