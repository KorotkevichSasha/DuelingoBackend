package by.gsu.duelingobackend.dto.response;

import jakarta.validation.constraints.NotBlank;

public record AddWordRequest(

        @NotBlank
        String term,

        @NotBlank
        String translation
) {
}
