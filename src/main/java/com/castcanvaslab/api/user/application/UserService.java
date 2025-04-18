package com.castcanvaslab.api.user.application;

import com.castcanvaslab.api.common.global.error.DomainException;
import com.castcanvaslab.api.common.global.error.ErrorCode;
import com.castcanvaslab.api.user.application.dto.UpdateNicknameRequest;
import com.castcanvaslab.api.user.application.dto.UserResponse;
import com.castcanvaslab.api.user.domain.User;
import com.castcanvaslab.api.user.domain.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUser(UUID userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateNickname(UUID userId, UpdateNicknameRequest request) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));
        user.changeNickname(request.nickname());
        return UserResponse.from(user);
    }
}
