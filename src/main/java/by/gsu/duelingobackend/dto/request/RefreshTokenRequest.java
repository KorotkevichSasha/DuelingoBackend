package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token can't be blank")
        String refreshToken
) {
}
