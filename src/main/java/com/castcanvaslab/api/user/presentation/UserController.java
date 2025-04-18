package com.castcanvaslab.api.user.presentation;

import com.castcanvaslab.api.common.global.api.ApiResponse;
import com.castcanvaslab.api.user.application.UserService;
import com.castcanvaslab.api.user.application.dto.UpdateNicknameRequest;
import com.castcanvaslab.api.user.application.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved", userService.getUser(userId)));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            Authentication authentication, @RequestBody @Valid UpdateNicknameRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Nickname updated", userService.updateNickname(userId, request)));
    }
}
