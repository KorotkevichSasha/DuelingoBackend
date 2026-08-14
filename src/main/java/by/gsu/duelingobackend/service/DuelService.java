package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.event.DuelResultEvent;
import by.gsu.duelingobackend.dto.request.DuelAnswerRequest;
import by.gsu.duelingobackend.dto.response.DuelAnswerReviewResponse;
import by.gsu.duelingobackend.dto.response.DuelInHistoryResponse;
import by.gsu.duelingobackend.dto.response.DuelResponse;
import by.gsu.duelingobackend.dto.response.DuelStatsResponse;
import by.gsu.duelingobackend.dto.response.PaginationResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.mapper.DuelMapper;
import by.gsu.duelingobackend.model.Duel;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.repository.DuelRepository;
import by.gsu.duelingobackend.repository.UserRepository;
import by.gsu.duelingobackend.repository.question.QuestionRepository;
import by.gsu.duelingobackend.service.matchmaking.EloRatingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
    private final ObjectMapper objectMapper;
    private final Map<UUID, CompletableFuture<Void>> pendingDuels = new ConcurrentHashMap<>();

    private static final int DEFAULT_QUESTIONS_SIZE = 10;

    public DuelResponse createDuel(UUID player1Id, UUID player2Id) {
        return createDuel(player1Id, player2Id, QuestionDifficulty.MEDIUM, DEFAULT_QUESTIONS_SIZE);
    }

    public DuelResponse createDuel(UUID player1Id, UUID player2Id, QuestionDifficulty difficulty, int questionCount) {
        log.info("Starting duel creation between players {} and {}",
                player1Id, player2Id);

        User player1 = getUserWithLogging(player1Id);
        User player2 = getUserWithLogging(player2Id);

        List<Question> questions = questionRepository.findRandomQuestions(
                null,
                difficulty,
                null,
                questionCount
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
        Page<Duel> duelsPage = duelRepository.findFinishedByUserId(userId, PageRequest.of(page, size));
        
        List<DuelInHistoryResponse> content = duelsPage.getContent().stream()
                .map(duel -> toHistoryResponse(duel, userId))
                .toList();
                
        return new PaginationResponse<>(
                content,
                duelsPage.getNumber(),
                duelsPage.getTotalPages(),
                duelsPage.getTotalElements()
        );
    }

    public void processDuelResults(
            UUID duelId,
            UUID userId,
            int score,
            long timeSpent,
            List<DuelAnswerRequest> answers
    ) {
        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new EntityNotFoundException("Duel not found"));

        int questionCount = duel.getQuestionIds() == null ? 0 : duel.getQuestionIds().length;
        if (score < 0 || score > questionCount) {
            throw new IllegalArgumentException("Score must be between 0 and " + questionCount);
        }
        if (timeSpent < 0 || timeSpent > 180_000) {
            throw new IllegalArgumentException("Time spent must be between 0 and 180000 ms");
        }

        if (duel.getEndedAt() != null) {
            log.info("Ignoring a repeated result for finished duel {}", duelId);
            return;
        }

        List<DuelAnswerRequest> safeAnswers = answers == null ? List.of() : sanitizeAnswers(duel, answers);
        int verifiedScore = safeAnswers.isEmpty() ? score : calculateScore(duel, safeAnswers);
        updatePlayerScore(duel, userId, verifiedScore, timeSpent, safeAnswers);

        if (bothPlayersResponded(duel)) {
            finishDuel(duelId);
            CompletableFuture<Void> timeout = pendingDuels.remove(duelId);
            if (timeout != null) {
                timeout.cancel(true);
            }
        } else {
            pendingDuels.computeIfAbsent(duelId, ignored -> CompletableFuture.runAsync(
                    () -> finishDuel(duelId),
                    CompletableFuture.delayedExecutor(200, TimeUnit.SECONDS)
            ));
        }
    }

    @Transactional
    public void finishDuel(UUID duelId) {
        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new EntityNotFoundException("Duel not found with ID: " + duelId));

        if (duel.getEndedAt() != null) {
            return;
        }

        if (duel.getPlayer1Score() == null) {
            duel.setPlayer1Score(0);
            duel.setPlayer1Time(0L);
        }
        if (duel.getPlayer2Score() == null) {
            duel.setPlayer2Score(0);
            duel.setPlayer2Time(0L);
        }

        completeDuel(duel, null);
    }

    @Transactional(readOnly = true)
    public DuelStatsResponse getUserDuelStats(UUID userId) {
        DuelRepository.DuelStatsProjection stats = duelRepository.getStatsByUserId(userId);
        long total = stats.getTotal();
        long wins = stats.getWins();
        int winRate = total == 0 ? 0 : (int) Math.round(wins * 100.0 / total);
        return new DuelStatsResponse(total, wins, stats.getLosses(), stats.getDraws(), winRate);
    }

    @Transactional
    public void forfeitDuel(UUID duelId, UUID userId) {
        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new EntityNotFoundException("Duel not found with ID: " + duelId));
        if (duel.getEndedAt() != null) return;

        String forfeitedBy;
        if (duel.getPlayer1().getId().equals(userId)) {
            forfeitedBy = duel.getPlayer1().getUsername();
            duel.setPlayer1Score(0);
            duel.setPlayer1Time(0L);
            duel.setPlayer2Score(Math.max(1, duel.getPlayer2Score() == null ? 0 : duel.getPlayer2Score()));
            if (duel.getPlayer2Time() == null) duel.setPlayer2Time(0L);
        } else if (duel.getPlayer2().getId().equals(userId)) {
            forfeitedBy = duel.getPlayer2().getUsername();
            duel.setPlayer2Score(0);
            duel.setPlayer2Time(0L);
            duel.setPlayer1Score(Math.max(1, duel.getPlayer1Score() == null ? 0 : duel.getPlayer1Score()));
            if (duel.getPlayer1Time() == null) duel.setPlayer1Time(0L);
        } else {
            throw new AccessDeniedException("User is not a participant of this duel");
        }

        CompletableFuture<Void> timeout = pendingDuels.remove(duelId);
        if (timeout != null) timeout.cancel(true);
        completeDuel(duel, forfeitedBy);
    }

    private void completeDuel(Duel duel, String forfeitedBy) {
        duel.setEndedAt(LocalDateTime.now());
        duelRepository.save(duel);
        eloRatingService.updateRatings(duel);
        achievementService.updateProgress(duel.getPlayer1().getId(), AchievementConditionType.DUEL_PLAYED, 1);
        achievementService.updateProgress(duel.getPlayer2().getId(), AchievementConditionType.DUEL_PLAYED, 1);
        sendDuelResult(duel, forfeitedBy);
    }

    private void updatePlayerScore(
            Duel duel,
            UUID userId,
            int score,
            long timeSpent,
            List<DuelAnswerRequest> answers
    ) {
        String serializedAnswers = serializeAnswers(answers);
        if (duel.getPlayer1().getId().equals(userId)) {
            duel.setPlayer1Score(score);
            duel.setPlayer1Time(timeSpent);
            duel.setPlayer1Answers(serializedAnswers);
        } else if (duel.getPlayer2().getId().equals(userId)) {
            duel.setPlayer2Score(score);
            duel.setPlayer2Time(timeSpent);
            duel.setPlayer2Answers(serializedAnswers);
        } else {
            throw new AccessDeniedException("User is not a participant of this duel");
        }
        duelRepository.save(duel);
    }

    public void submitSimulatedResult(UUID duelId, UUID opponentId, QuestionDifficulty difficulty) {
        Duel duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new EntityNotFoundException("Duel not found"));
        int total = duel.getQuestionIds() == null ? 0 : duel.getQuestionIds().length;
        double accuracy = switch (difficulty) {
            case EASY -> 0.48;
            case MEDIUM -> 0.68;
            case HARD -> 0.82;
        };
        int variance = ThreadLocalRandom.current().nextInt(-1, 2);
        int score = Math.max(0, Math.min(total, (int) Math.round(total * accuracy) + variance));
        long time = switch (difficulty) {
            case EASY -> ThreadLocalRandom.current().nextLong(95_000L, 155_000L);
            case MEDIUM -> ThreadLocalRandom.current().nextLong(65_000L, 105_000L);
            case HARD -> ThreadLocalRandom.current().nextLong(48_000L, 72_000L);
        };
        List<Question> questions = orderedDuelQuestions(duel);
        List<Integer> indexes = new ArrayList<>();
        for (int index = 0; index < questions.size(); index++) indexes.add(index);
        Collections.shuffle(indexes);
        Set<Integer> correctIndexes = new HashSet<>(indexes.subList(0, Math.min(score, indexes.size())));
        List<DuelAnswerRequest> simulatedAnswers = new ArrayList<>();
        for (int index = 0; index < questions.size(); index++) {
            Question question = questions.get(index);
            String answer = correctIndexes.contains(index)
                    ? answerAcceptedByQuestion(question)
                    : question.getOptions().stream()
                            .filter(option -> !isCorrect(question, option))
                            .findFirst()
                            .orElse("—");
            simulatedAnswers.add(new DuelAnswerRequest(question.getId().toString(), answer));
        }
        processDuelResults(duelId, opponentId, score, time, simulatedAnswers);
    }

    private DuelInHistoryResponse toHistoryResponse(Duel duel, UUID currentUserId) {
        List<DuelAnswerReviewResponse> player1Review = buildReview(duel, duel.getPlayer1Answers());
        List<DuelAnswerReviewResponse> player2Review = buildReview(duel, duel.getPlayer2Answers());
        boolean currentUserIsPlayer1 = duel.getPlayer1().getId().equals(currentUserId);
        return duelMapper.toDuelInHistoryResponse(
                duel,
                currentUserIsPlayer1 ? player1Review : player2Review,
                currentUserIsPlayer1 ? player2Review : player1Review
        );
    }

    private List<DuelAnswerRequest> sanitizeAnswers(Duel duel, List<DuelAnswerRequest> answers) {
        Set<String> allowedIds = Set.of(duel.getQuestionIds());
        Map<String, DuelAnswerRequest> unique = new LinkedHashMap<>();
        for (DuelAnswerRequest answer : answers) {
            if (answer != null && answer.questionId() != null && allowedIds.contains(answer.questionId())) {
                unique.putIfAbsent(answer.questionId(), new DuelAnswerRequest(
                        answer.questionId(),
                        answer.submittedAnswer() == null ? "" : answer.submittedAnswer().trim()
                ));
            }
        }
        return List.copyOf(unique.values());
    }

    private int calculateScore(Duel duel, List<DuelAnswerRequest> answers) {
        Map<String, String> submittedByQuestion = new HashMap<>();
        answers.forEach(answer -> submittedByQuestion.put(answer.questionId(), answer.submittedAnswer()));
        return loadDuelQuestions(duel).stream()
                .filter(question -> isCorrect(question, submittedByQuestion.getOrDefault(question.getId().toString(), "")))
                .mapToInt(ignored -> 1)
                .sum();
    }

    private List<DuelAnswerReviewResponse> buildReview(Duel duel, String serializedAnswers) {
        if (serializedAnswers == null || serializedAnswers.isBlank()) {
            return List.of();
        }
        Map<String, String> submittedByQuestion = new HashMap<>();
        deserializeAnswers(serializedAnswers)
                .forEach(answer -> submittedByQuestion.put(answer.questionId(), answer.submittedAnswer()));

        Map<String, Question> questionsById = new HashMap<>();
        loadDuelQuestions(duel).forEach(question -> questionsById.put(question.getId().toString(), question));

        List<DuelAnswerReviewResponse> review = new ArrayList<>();
        for (int index = 0; index < duel.getQuestionIds().length; index++) {
            String questionId = duel.getQuestionIds()[index];
            Question question = questionsById.get(questionId);
            if (question == null) {
                continue;
            }
            String submitted = submittedByQuestion.getOrDefault(questionId, "");
            review.add(new DuelAnswerReviewResponse(
                    index + 1,
                    question.getQuestionText(),
                    question.getType(),
                    submitted,
                    correctAnswer(question),
                    isCorrect(question, submitted)
            ));
        }
        return review;
    }

    private List<Question> loadDuelQuestions(Duel duel) {
        if (duel.getQuestionIds() == null || duel.getQuestionIds().length == 0) {
            return List.of();
        }
        return questionRepository.findAllById(Arrays.asList(duel.getQuestionIds()));
    }

    private List<Question> orderedDuelQuestions(Duel duel) {
        Map<String, Question> byId = new HashMap<>();
        loadDuelQuestions(duel).forEach(question -> byId.put(question.getId().toString(), question));
        return Arrays.stream(duel.getQuestionIds())
                .map(byId::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private boolean isCorrect(Question question, String submittedAnswer) {
        String submitted = normalizeAnswer(submittedAnswer);
        if (question.getType() == by.gsu.duelingobackend.model.enums.QuestionType.SENTENCE_CONSTRUCTION) {
            return submitted.equals(normalizeAnswer(String.join(" ", question.getCorrectAnswers())));
        }
        return question.getCorrectAnswers().stream()
                .map(this::normalizeAnswer)
                .anyMatch(submitted::equals);
    }

    private String correctAnswer(Question question) {
        return question.getType() == by.gsu.duelingobackend.model.enums.QuestionType.SENTENCE_CONSTRUCTION
                ? String.join(" ", question.getCorrectAnswers())
                : String.join(" / ", question.getCorrectAnswers());
    }

    private String answerAcceptedByQuestion(Question question) {
        return question.getType() == by.gsu.duelingobackend.model.enums.QuestionType.SENTENCE_CONSTRUCTION
                ? String.join(" ", question.getCorrectAnswers())
                : question.getCorrectAnswers().stream().findFirst().orElse("");
    }

    private String normalizeAnswer(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replaceAll("[.,!?;:]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String serializeAnswers(List<DuelAnswerRequest> answers) {
        if (answers.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot save duel answers", exception);
        }
    }

    private List<DuelAnswerRequest> deserializeAnswers(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() { });
        } catch (JsonProcessingException exception) {
            log.warn("Cannot read stored duel answers", exception);
            return List.of();
        }
    }

    private void sendDuelResult(Duel duel, String forfeitedBy) {
        String winner;

        if (duel.getPlayer1Score() > duel.getPlayer2Score()) {
            winner = duel.getPlayer1().getUsername();
        } else if (duel.getPlayer2Score() > duel.getPlayer1Score()) {
            winner = duel.getPlayer2().getUsername();
        } else {
            winner = "Draw";
        }

        DuelResultEvent resultEvent = new DuelResultEvent(
                duel.getPlayer1Score(),
                duel.getPlayer2Score(),
                winner,
                forfeitedBy
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
                .player1Score(null)
                .player2Score(null)
                .build();
    }

    private String[] mapQuestionIds(List<Question> questions) {
        return questions.stream()
                .map(Question::getId)
                .map(ObjectId::toString)
                .toArray(String[]::new);
    }
}
