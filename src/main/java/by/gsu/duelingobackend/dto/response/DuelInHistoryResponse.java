package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.dto.response.user.UserInDuelResponse;

import java.util.UUID;

public record DuelInHistoryResponse(
        UUID id,
        UserInDuelResponse player1,
        int player1Score,
        long player1Time,
        UserInDuelResponse player2,
        int player2Score,
        long player2Time
) {
}
