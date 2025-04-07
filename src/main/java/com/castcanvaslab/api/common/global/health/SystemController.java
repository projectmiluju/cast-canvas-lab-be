package com.castcanvaslab.api.common.global.health;

import com.castcanvaslab.api.common.global.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

	private final SystemService systemService;

	public SystemController(SystemService systemService) {
		this.systemService = systemService;
	}

	@GetMapping("/ping")
	public ApiResponse<SystemStatus> ping() {
		return ApiResponse.success("System is reachable", systemService.getStatus());
	}
}
