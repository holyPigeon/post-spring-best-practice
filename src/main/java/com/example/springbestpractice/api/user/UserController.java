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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping("/me")
    public UserResponse getMyProfile(@CurrentUser LoginUser loginUser) {
        return userService.getMyProfile(loginUser);
    }

    @PutMapping("/me")
    public UserResponse updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request,
            @CurrentUser LoginUser loginUser
    ) {
        return userService.updateMyProfile(UserUpdateCommand.from(request, loginUser));
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(
            @Valid @RequestBody UserPasswordUpdateRequest request,
            @CurrentUser LoginUser loginUser
    ) {
        userService.updatePassword(UserPasswordUpdateCommand.from(request, loginUser));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyAccount(@CurrentUser LoginUser loginUser) {
        userService.deleteMyAccount(UserDeleteCommand.from(loginUser));
    }
}
