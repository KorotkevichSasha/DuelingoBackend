package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DuelAnswerRequest(
        @NotBlank String questionId,
        String submittedAnswer
) {
}
