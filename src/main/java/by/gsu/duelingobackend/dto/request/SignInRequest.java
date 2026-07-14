package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequest(

        @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters long")
        @NotBlank(message = "Username cannot be empty")
        String username,

        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
        @NotBlank(message = "Password cannot be empty")
        String password
) {
}
