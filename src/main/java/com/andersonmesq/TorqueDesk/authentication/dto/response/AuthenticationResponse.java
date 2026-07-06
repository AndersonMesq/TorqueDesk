package com.andersonmesq.TorqueDesk.authentication.dto.response;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken
) {
}
