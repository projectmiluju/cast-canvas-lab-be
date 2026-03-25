package com.castcanvaslab.api.workspace.application;

import com.castcanvaslab.api.common.global.error.DomainException;
import com.castcanvaslab.api.common.global.error.ErrorCode;
import com.castcanvaslab.api.workspace.application.dto.CreateWorkspaceRequest;
import com.castcanvaslab.api.workspace.application.dto.UpdateWorkspaceRequest;
import com.castcanvaslab.api.workspace.application.dto.WorkspaceResponse;
import com.castcanvaslab.api.workspace.domain.Workspace;
import com.castcanvaslab.api.workspace.domain.WorkspaceRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public WorkspaceResponse createWorkspace(UUID ownerId, CreateWorkspaceRequest request) {
        Workspace workspace = Workspace.create(ownerId, request.name(), request.description());
        return WorkspaceResponse.from(workspaceRepository.save(workspace));
    }

    public List<WorkspaceResponse> getMyWorkspaces(UUID ownerId) {
        return workspaceRepository.findAllByOwnerId(ownerId).stream()
                .map(WorkspaceResponse::from)
                .toList();
    }

    public WorkspaceResponse getWorkspace(UUID userId, UUID workspaceId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        validateOwner(workspace, userId);
        return WorkspaceResponse.from(workspace);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(
            UUID userId, UUID workspaceId, UpdateWorkspaceRequest request) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        validateOwner(workspace, userId);
        workspace.update(request.name(), request.description());
        return WorkspaceResponse.from(workspace);
    }

    @Transactional
    public void deleteWorkspace(UUID userId, UUID workspaceId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        validateOwner(workspace, userId);
        workspaceRepository.delete(workspace);
    }

    private Workspace findWorkspaceOrThrow(UUID workspaceId) {
        return workspaceRepository
                .findById(workspaceId)
                .orElseThrow(() -> new DomainException(ErrorCode.WORKSPACE_NOT_FOUND));
    }

    private void validateOwner(Workspace workspace, UUID userId) {
        if (!workspace.isOwnedBy(userId)) {
            throw new DomainException(ErrorCode.WORKSPACE_FORBIDDEN);
        }
    }
}
