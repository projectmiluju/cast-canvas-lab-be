package com.castcanvaslab.api.workspace.presentation;

import com.castcanvaslab.api.common.global.api.ApiResponse;
import com.castcanvaslab.api.workspace.application.WorkspaceService;
import com.castcanvaslab.api.workspace.application.dto.CreateWorkspaceRequest;
import com.castcanvaslab.api.workspace.application.dto.UpdateWorkspaceRequest;
import com.castcanvaslab.api.workspace.application.dto.WorkspaceResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            Authentication authentication, @RequestBody @Valid CreateWorkspaceRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        WorkspaceResponse response = workspaceService.createWorkspace(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getMyWorkspaces(
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<WorkspaceResponse> response = workspaceService.getMyWorkspaces(userId);
        return ResponseEntity.ok(ApiResponse.success("Workspaces retrieved", response));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(
            Authentication authentication, @PathVariable UUID workspaceId) {
        UUID userId = UUID.fromString(authentication.getName());
        WorkspaceResponse response = workspaceService.getWorkspace(userId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Workspace retrieved", response));
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @RequestBody @Valid UpdateWorkspaceRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        WorkspaceResponse response = workspaceService.updateWorkspace(userId, workspaceId, request);
        return ResponseEntity.ok(ApiResponse.success("Workspace updated", response));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            Authentication authentication, @PathVariable UUID workspaceId) {
        UUID userId = UUID.fromString(authentication.getName());
        workspaceService.deleteWorkspace(userId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted", null));
    }
}
