package com.castcanvaslab.api.auth.application.dto;

public record TokenResponse(String accessToken, String refreshToken) {}
