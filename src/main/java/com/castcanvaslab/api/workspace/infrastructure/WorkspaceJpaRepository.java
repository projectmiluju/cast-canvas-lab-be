package com.castcanvaslab.api.workspace.infrastructure;

import com.castcanvaslab.api.workspace.domain.Workspace;
import com.castcanvaslab.api.workspace.domain.WorkspaceRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceJpaRepository
        extends JpaRepository<Workspace, UUID>, WorkspaceRepository {}
