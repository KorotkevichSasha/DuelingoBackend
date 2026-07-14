package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.dto.response.user.UserInDuelResponse;

import java.util.List;
import java.util.UUID;

public record DuelResponse(

        UUID id,
        UserInDuelResponse player1,
        UserInDuelResponse player2,
        List<QuestionDetailedResponse> questions
) {
}
