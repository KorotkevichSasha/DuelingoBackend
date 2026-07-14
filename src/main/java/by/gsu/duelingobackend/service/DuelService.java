package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.event.DuelResultEvent;
import by.gsu.duelingobackend.dto.response.DuelInHistoryResponse;
import by.gsu.duelingobackend.dto.response.DuelResponse;
import by.gsu.duelingobackend.dto.response.PaginationResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.mapper.DuelMapper;
import by.gsu.duelingobackend.model.Duel;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.repository.DuelRepository;
import by.gsu.duelingobackend.repository.UserRepository;
import by.gsu.duelingobackend.repository.question.QuestionRepository;
import by.gsu.duelingobackend.service.matchmaking.EloRatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static by.gsu.duelingobackend.util.Constants.USER_NOT_FOUND_BY_ID_ERR_MSG;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuelService {

    private final EloRatingService eloRatingService;
    private final DuelRepository duelRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final DuelMapper duelMapper;
    private final AchievementService achievementService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<UUID, CompletableFuture<Void>> pendingDuels = new ConcurrentHashMap<>();

    private static final int DEFAULT_QUESTIONS_SIZE = 10;

    public DuelResponse createDuel(UUID player1Id, UUID player2Id) {
        log.info("Starting duel creation between players {} and {}",
                player1Id, player2Id);

        User player1 = getUserWithLogging(player1Id);
        User player2 = getUserWithLogging(player2Id);

        List<Question> questions = questionRepository.findRandomQuestions(
                null,
                null,
                null,
                DEFAULT_QUESTIONS_SIZE
        );

        if (questions.isEmpty()) {
            log.warn("No questions found");
        } else {
            log.info("Selected {} questions for duel", questions.size());
        }

        Duel duel = buildDuel(player1, player2, questions);
        Duel savedDuel = duelRepository.save(duel);

        log.info("Duel created successfully with ID: {}", savedDuel.getId());
        return duelMapper.toResponse(savedDuel, questions);
    }

    public PaginationResponse<DuelInHistoryResponse> getUserDuelHistory(UUID userId, int page, int size) {
        Page<Duel> duelsPage = duelRepository.findAllByPlayer1IdOrPlayer2IdOrderByStartedAtDesc(
                userId, 
                userId, 
                PageRequest.of(page, size)
        );
        
        List<DuelInHistoryResponse> content = duelsPage.getContent().stream()
                .map(duelMapper::toDuelInHistoryResponse)
                .toList();
                
        return new PaginationResponse<>(
                content,
                duelsPage.getNumber(),
                duelsPage.getTotalPages(),
                duelsPage.getTotalElements()
        );
    }

    public void processDuelResults(UUID duelId, UUID userId, int score, long timeSpent) {
        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new EntityNotFoundException("Duel not found"));

        updatePlayerScore(duel, userId, score, timeSpent);

        if (bothPlayersResponded(duel)) {
            finishDuel(duelId);
            pendingDuels.remove(duelId);
        } else {
            pendingDuels.put(duelId, CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(120_000);
                    finishDuel(duelId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
    }

    @Transactional
    public void finishDuel(UUID duelId) {
        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new EntityNotFoundException("Duel not found with ID: " + duelId));

        duel.setEndedAt(LocalDateTime.now());
        duelRepository.save(duel);

        eloRatingService.updateRatings(duel);
        achievementService.updateProgress(duel.getPlayer1().getId(), AchievementConditionType.DUEL_PLAYED, 1);
        achievementService.updateProgress(duel.getPlayer2().getId(), AchievementConditionType.DUEL_PLAYED, 1);
        sendDuelResult(duel);
    }

    private void updatePlayerScore(Duel duel, UUID userId, int score, long timeSpent) {
        if (duel.getPlayer1().getId().equals(userId)) {
            duel.setPlayer1Score(score);
            duel.setPlayer1Time(timeSpent);
        } else {
            duel.setPlayer2Score(score);
            duel.setPlayer2Time(timeSpent);
        }
        duelRepository.save(duel);
    }

    private void sendDuelResult(Duel duel) {
        String winner;

        if (duel.getPlayer1Score() > duel.getPlayer2Score()) {
            winner = duel.getPlayer1().getUsername();
        } else if (duel.getPlayer2Score() > duel.getPlayer1Score()) {
            winner = duel.getPlayer2().getUsername();
        } else {
            winner = "Draw";
        }

        DuelResultEvent resultEvent = new DuelResultEvent(
                duel.getPlayer1().getPoints(),
                duel.getPlayer2().getPoints(),
                winner
        );

        messagingTemplate.convertAndSendToUser(
                duel.getPlayer1().getUsername(),
                "/queue/duel-result",
                resultEvent
        );
        messagingTemplate.convertAndSendToUser(
                duel.getPlayer2().getUsername(),
                "/queue/duel-result",
                resultEvent
        );
    }

    private boolean bothPlayersResponded(Duel duel) {
        return duel.getPlayer1Score() != null
                && duel.getPlayer2Score() != null;
    }

    private User getUserWithLogging(UUID playerId) {
        return userRepository.findById(playerId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", playerId);
                    return new EntityNotFoundException(
                            String.format(USER_NOT_FOUND_BY_ID_ERR_MSG, playerId)
                    );
                });
    }

    private Duel buildDuel(User player1, User player2, List<Question> questions) {
        return Duel.builder()
                .player1(player1)
                .player2(player2)
                .questionIds(mapQuestionIds(questions))
                .startedAt(LocalDateTime.now())
                .player1Score(0)
                .player2Score(0)
                .build();
    }

    private String[] mapQuestionIds(List<Question> questions) {
        return questions.stream()
                .map(Question::getId)
                .map(ObjectId::toString)
                .toArray(String[]::new);
    }
}
