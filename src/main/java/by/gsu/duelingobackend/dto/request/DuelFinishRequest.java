package by.gsu.duelingobackend.dto.request;

import java.util.UUID;

public record DuelFinishRequest(
        UUID duelId,
        int correctAnswers,
        long timeSpent
) {}
