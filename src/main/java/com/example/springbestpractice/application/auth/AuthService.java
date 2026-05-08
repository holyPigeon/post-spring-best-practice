package com.example.springbestpractice.application.auth;

import com.example.springbestpractice.api.auth.dto.LoginRequest;
import com.example.springbestpractice.api.auth.dto.RefreshRequest;
import com.example.springbestpractice.api.auth.dto.TokenResponse;
import com.example.springbestpractice.domain.auth.ExpiredRefreshTokenException;
import com.example.springbestpractice.domain.auth.RefreshToken;
import com.example.springbestpractice.domain.auth.RefreshTokenNotFoundException;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.infrastructure.auth.RefreshTokenRepository;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import com.example.springbestpractice.infrastructure.security.JwtTokenProvider;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return issueTokenPair(userDetails.getUser());
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(RefreshTokenNotFoundException::new);

        refreshTokenRepository.delete(refreshToken);

        if (refreshToken.isExpired()) {
            throw new ExpiredRefreshTokenException();
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException(refreshToken.getUserId()));

        return issueTokenPair(user);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    private TokenResponse issueTokenPair(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String rawRefreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.create(user.getId(), rawRefreshToken, jwtTokenProvider.getRefreshTokenExpiration()));
        return TokenResponse.of(accessToken, rawRefreshToken);
    }
}
