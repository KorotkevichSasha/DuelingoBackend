package by.gsu.duelingobackend.dto.request;

import java.util.UUID;
import java.util.List;

public record DuelFinishRequest(
        UUID duelId,
        int correctAnswers,
        long timeSpent,
        List<DuelAnswerRequest> answers
) {}
