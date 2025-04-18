package com.castcanvaslab.api.user.application.dto;

import com.castcanvaslab.api.user.domain.User;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(UUID id, String email, String nickname, OffsetDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getNickname(), user.getCreatedAt());
    }
}
