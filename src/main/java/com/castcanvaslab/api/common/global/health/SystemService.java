package com.castcanvaslab.api.common.global.health;

import org.springframework.stereotype.Service;

@Service
public class SystemService {

    public SystemStatus getStatus() {
        return new SystemStatus("cast-canvas-lab-be", "UP");
    }
}
