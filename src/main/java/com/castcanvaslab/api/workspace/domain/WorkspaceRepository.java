package com.castcanvaslab.api.workspace.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {

    List<Workspace> findAllByOwnerId(UUID ownerId);

    Optional<Workspace> findById(UUID id);

    Workspace save(Workspace workspace);

    void delete(Workspace workspace);
}
