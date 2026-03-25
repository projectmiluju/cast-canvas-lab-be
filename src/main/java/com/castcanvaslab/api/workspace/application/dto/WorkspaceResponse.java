package com.castcanvaslab.api.workspace.application.dto;

import com.castcanvaslab.api.workspace.domain.Workspace;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getOwnerId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt());
    }
}
