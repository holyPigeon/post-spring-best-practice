package com.example.springbestpractice.api.auth;

import com.example.springbestpractice.api.auth.dto.LoginRequest;
import com.example.springbestpractice.api.auth.dto.TokenResponse;
import com.example.springbestpractice.common.annotation.CurrentUser;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import com.example.springbestpractice.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.createToken(
                userDetails.getUser().getId(),
                userDetails.getUser().getEmail()
        );
        return new TokenResponse(token);
    }

    @GetMapping("/me")
    public LoginUser me(@CurrentUser LoginUser loginUser) {
        return loginUser;
    }
}
