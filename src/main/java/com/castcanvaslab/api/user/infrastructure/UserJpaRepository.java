package com.castcanvaslab.api.user.infrastructure;

import com.castcanvaslab.api.user.domain.User;
import com.castcanvaslab.api.user.domain.UserRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, UUID>, UserRepository {}
