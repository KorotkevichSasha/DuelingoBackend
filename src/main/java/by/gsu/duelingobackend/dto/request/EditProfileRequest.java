package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditProfileRequest(

        @NotBlank(message = "Email address cannot be empty")
        @Email(message = "Email address must be in the format user@example.com")
        @Size(min = 5, max = 255, message = "Email address must be between 5 and 255 characters long")
        String email

) {
}
