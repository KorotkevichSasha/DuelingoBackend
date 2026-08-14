package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.DuelAnswerRequest;
import by.gsu.duelingobackend.mapper.DuelMapper;
import by.gsu.duelingobackend.model.Duel;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import by.gsu.duelingobackend.repository.DuelRepository;
import by.gsu.duelingobackend.repository.UserRepository;
import by.gsu.duelingobackend.repository.question.QuestionRepository;
import by.gsu.duelingobackend.service.matchmaking.EloRatingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuelServiceTest {

    @Mock private EloRatingService eloRatingService;
    @Mock private DuelRepository duelRepository;
    @Mock private UserRepository userRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private DuelMapper duelMapper;
    @Mock private AchievementService achievementService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @Test
    void verifiesSubmittedAnswersInsteadOfTrustingTheClientScore() {
        UUID duelId = UUID.randomUUID();
        UUID player1Id = UUID.randomUUID();
        ObjectId questionId = new ObjectId();
        User player1 = User.builder().id(player1Id).username("PlayerOne").build();
        User player2 = User.builder().id(UUID.randomUUID()).username("PlayerTwo").build();
        Duel duel = Duel.builder()
                .id(duelId)
                .player1(player1)
                .player2(player2)
                .player2Score(0)
                .player2Time(1L)
                .questionIds(new String[]{questionId.toString()})
                .build();
        Question question = Question.builder()
                .id(questionId)
                .topic("Present Simple")
                .difficulty(QuestionDifficulty.EASY)
                .type(QuestionType.FILL_IN_INPUT)
                .questionText("Anna ___ English every week.")
                .options(List.of("studies", "study"))
                .correctAnswers(List.of("studies"))
                .build();

        when(duelRepository.findById(duelId)).thenReturn(Optional.of(duel));
        when(questionRepository.findAllById(List.of(questionId.toString()))).thenReturn(List.of(question));

        DuelService service = new DuelService(
                eloRatingService,
                duelRepository,
                userRepository,
                questionRepository,
                duelMapper,
                achievementService,
                messagingTemplate,
                new ObjectMapper()
        );

        service.processDuelResults(
                duelId,
                player1Id,
                1,
                5_000,
                List.of(new DuelAnswerRequest(questionId.toString(), "study"))
        );

        assertThat(duel.getPlayer1Score()).isZero();
        assertThat(duel.getPlayer1Answers()).contains("study");
    }

    @Test
    void forfeitImmediatelyFinishesDuelAndNotifiesOpponent() {
        UUID duelId = UUID.randomUUID();
        User player1 = User.builder().id(UUID.randomUUID()).username("PlayerOne").build();
        User player2 = User.builder().id(UUID.randomUUID()).username("PlayerTwo").build();
        Duel duel = Duel.builder()
                .id(duelId)
                .player1(player1)
                .player2(player2)
                .questionIds(new String[0])
                .build();
        when(duelRepository.findById(duelId)).thenReturn(Optional.of(duel));

        DuelService service = new DuelService(
                eloRatingService,
                duelRepository,
                userRepository,
                questionRepository,
                duelMapper,
                achievementService,
                messagingTemplate,
                new ObjectMapper()
        );

        service.forfeitDuel(duelId, player1.getId());

        assertThat(duel.getEndedAt()).isNotNull();
        assertThat(duel.getPlayer1Score()).isZero();
        assertThat(duel.getPlayer2Score()).isEqualTo(1);
        verify(messagingTemplate).convertAndSendToUser(
                eq("PlayerTwo"), eq("/queue/duel-result"), any());
    }
}
