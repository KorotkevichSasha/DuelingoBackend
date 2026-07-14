package by.gsu.duelingobackend.dto.event;

import by.gsu.duelingobackend.dto.response.DuelResponse;

import java.util.UUID;

public record DuelFoundEvent(
        DuelResponse duel,
        UUID opponentId
) {
}
