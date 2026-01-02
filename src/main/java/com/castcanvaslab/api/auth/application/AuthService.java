package com.castcanvaslab.api.auth.application;

import com.castcanvaslab.api.auth.application.dto.LoginRequest;
import com.castcanvaslab.api.auth.application.dto.SignupRequest;
import com.castcanvaslab.api.auth.application.dto.TokenResponse;
import com.castcanvaslab.api.auth.infrastructure.JwtTokenProvider;
import com.castcanvaslab.api.common.global.error.DomainException;
import com.castcanvaslab.api.common.global.error.ErrorCode;
import com.castcanvaslab.api.user.application.dto.UserResponse;
import com.castcanvaslab.api.user.domain.User;
import com.castcanvaslab.api.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DomainException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = User.create(request.email(), hashedPassword, request.nickname());
        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    public TokenResponse login(LoginRequest request) {
        User user =
                userRepository
                        .findByEmail(request.email())
                        .orElseThrow(() -> new DomainException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new DomainException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new TokenResponse(accessToken, refreshToken);
    }
}
