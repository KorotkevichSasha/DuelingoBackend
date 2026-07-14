package by.gsu.duelingobackend.dto.response;

public record JwtAuthenticationResponse(
        String accessToken,
        String refreshToken
) {}
