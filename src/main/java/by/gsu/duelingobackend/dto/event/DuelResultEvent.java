package by.gsu.duelingobackend.dto.event;

public record DuelResultEvent(
        int player1Score,
        int player2Score,
        String winner
) {
}
