package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.document.Test;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import by.gsu.duelingobackend.repository.question.QuestionRepository;
import by.gsu.duelingobackend.repository.test.TestRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.content",
        name = "bootstrap-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ContentBootstrapService implements ApplicationRunner {

    private static final String LISTENING_TOPIC = "Listening";
    private static final int QUESTIONS_PER_TEST = 10;

    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    @Value("classpath:data/questions.json")
    private Resource questionsResource;

    @Override
    public void run(ApplicationArguments args) {
        List<Question> bundledQuestions = reconcileQuestions(loadQuestions(), questionRepository.findAll());
        reconcileTests(buildTests(bundledQuestions), testRepository.findAll());

        Cache testsCache = cacheManager.getCache("tests");
        if (testsCache != null) {
            testsCache.clear();
        }
    }

    private List<Question> reconcileQuestions(List<Question> bundled, List<Question> existing) {
        Map<QuestionKey, Question> existingByKey = new LinkedHashMap<>();
        existing.forEach(question -> existingByKey.putIfAbsent(QuestionKey.from(question), question));
        Map<SentenceKey, Question> existingSentences = new LinkedHashMap<>();
        existing.stream()
                .filter(question -> question.getType() == QuestionType.SENTENCE_CONSTRUCTION)
                .forEach(question -> existingSentences.putIfAbsent(SentenceKey.from(question), question));

        Map<QuestionKey, Question> resolved = new LinkedHashMap<>();
        List<Question> toSave = new ArrayList<>();
        int updatedPrompts = 0;
        for (Question desired : bundled) {
            Question current = existingByKey.get(QuestionKey.from(desired));
            if (current == null && desired.getType() == QuestionType.SENTENCE_CONSTRUCTION) {
                current = existingSentences.get(SentenceKey.from(desired));
                if (current != null) {
                    current.setQuestionText(desired.getQuestionText());
                    current.setOptions(desired.getOptions());
                    current.setCorrectAnswers(desired.getCorrectAnswers());
                    toSave.add(current);
                    updatedPrompts++;
                }
            }
            if (current == null) {
                current = desired;
                toSave.add(current);
            }
            resolved.put(QuestionKey.from(desired), current);
        }
        if (!toSave.isEmpty()) {
            questionRepository.saveAll(toSave);
        }

        Set<Question> resolvedQuestions = new LinkedHashSet<>(resolved.values());
        Set<TestKey> bundledScopes = bundled.stream()
                .filter(question -> question.getType() != QuestionType.AUDIO_RECOGNITION)
                .map(question -> new TestKey(question.getTopic(), question.getDifficulty()))
                .collect(java.util.stream.Collectors.toSet());
        List<Question> obsoleteSentenceDuplicates = existing.stream()
                .filter(question -> !resolvedQuestions.contains(question))
                .filter(question -> question.getType() == QuestionType.AUDIO_RECOGNITION
                        || bundledScopes.contains(new TestKey(question.getTopic(), question.getDifficulty())))
                .toList();
        if (!obsoleteSentenceDuplicates.isEmpty()) {
            questionRepository.deleteAll(obsoleteSentenceDuplicates);
        }

        List<Question> reconciled = bundled.stream()
                .map(question -> resolved.get(QuestionKey.from(question)))
                .toList();
        log.info("Learning content ready: {} bundled questions ({} saved, {} prompts updated, {} duplicates removed)",
                reconciled.size(), toSave.size(), updatedPrompts, obsoleteSentenceDuplicates.size());
        return reconciled;
    }

    private void reconcileTests(List<Test> desiredTests, List<Test> existingTests) {
        Set<TestKey> desiredKeys = desiredTests.stream()
                .map(test -> new TestKey(test.getTopic(), test.getDifficulty()))
                .collect(java.util.stream.Collectors.toSet());
        Map<TestKey, Test> existingByKey = new LinkedHashMap<>();
        List<Test> obsoleteTests = new ArrayList<>();
        existingTests.stream()
                .sorted(Comparator.comparing(test -> test.getId() == null ? "" : test.getId().toString()))
                .forEach(test -> {
                    TestKey key = new TestKey(test.getTopic(), test.getDifficulty());
                    if (!desiredKeys.contains(key) || existingByKey.containsKey(key)) {
                        obsoleteTests.add(test);
                    } else {
                        existingByKey.put(key, test);
                    }
                });

        if (!obsoleteTests.isEmpty()) {
            testRepository.deleteAll(obsoleteTests);
        }

        List<Test> testsToSave = desiredTests.stream().map(desired -> {
            Test current = existingByKey.get(new TestKey(desired.getTopic(), desired.getDifficulty()));
            if (current == null) {
                return desired;
            }
            current.setQuestions(desired.getQuestions());
            return current;
        }).toList();

        testRepository.saveAll(testsToSave);
        log.info("Grammar catalogue ready: {} tests, {} questions in each ({} obsolete removed)",
                testsToSave.size(), QUESTIONS_PER_TEST, obsoleteTests.size());
    }

    private List<Question> loadQuestions() {
        try {
            List<SeedQuestion> seeds = objectMapper.readValue(
                    questionsResource.getInputStream(),
                    new TypeReference<>() {
                    }
            );

            Map<QuestionKey, Question> uniqueQuestions = new LinkedHashMap<>();
            for (SeedQuestion seed : seeds) {
                Question question = toQuestion(seed);
                uniqueQuestions.putIfAbsent(QuestionKey.from(question), question);
            }

            if (uniqueQuestions.isEmpty()) {
                throw new IllegalStateException("The bundled question dataset is empty");
            }
            return new ArrayList<>(uniqueQuestions.values());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read bundled learning questions", exception);
        }
    }

    private Question toQuestion(SeedQuestion seed) {
        if (seed.type() == null || !StringUtils.hasText(seed.questionText())) {
            throw new IllegalStateException("A bundled question has no type or text");
        }

        if (seed.type() == QuestionType.AUDIO_RECOGNITION) {
            String text = seed.questionText().trim();
            return Question.builder()
                    .topic(LISTENING_TOPIC)
                    .difficulty(QuestionDifficulty.MEDIUM)
                    .type(seed.type())
                    .questionText(text)
                    .options(List.of(text))
                    .correctAnswers(List.of(text))
                    .audioUrl(seed.audioUrl())
                    .build();
        }

        if (!StringUtils.hasText(seed.topic())
                || seed.difficulty() == null
                || seed.options() == null
                || (seed.type() != QuestionType.FILL_IN_INPUT && seed.options().isEmpty())
                || seed.correctAnswers() == null
                || seed.correctAnswers().isEmpty()) {
            throw new IllegalStateException("A bundled grammar question is incomplete: " + seed.questionText());
        }

        if (seed.type() == QuestionType.FILL_IN_INPUT
                && !seed.questionText().contains("(подсказка:")) {
            throw new IllegalStateException(
                    "A free-input question must contain a semantic or grammar hint: " + seed.questionText()
            );
        }

        return Question.builder()
                .topic(seed.topic().trim())
                .difficulty(seed.difficulty())
                .type(seed.type())
                .questionText(seed.questionText().trim())
                .options(List.copyOf(seed.options()))
                .correctAnswers(List.copyOf(seed.correctAnswers()))
                .audioUrl(seed.audioUrl())
                .build();
    }

    private List<Test> buildTests(List<Question> questions) {
        Map<TestKey, List<Question>> questionsByTest = new LinkedHashMap<>();
        questions.stream()
                .filter(question -> question.getType() != QuestionType.AUDIO_RECOGNITION)
                .forEach(question -> questionsByTest
                        .computeIfAbsent(
                                new TestKey(question.getTopic(), question.getDifficulty()),
                                ignored -> new ArrayList<>()
                        )
                        .add(question));

        List<Test> tests = questionsByTest.entrySet().stream()
                .map(entry -> Test.builder()
                        .topic(entry.getKey().topic())
                        .difficulty(entry.getKey().difficulty())
                        .questions(uniqueQuestions(entry.getValue(), QUESTIONS_PER_TEST))
                        .build())
                .toList();

        tests.forEach(test -> {
            if (test.getQuestions().size() != QUESTIONS_PER_TEST) {
                throw new IllegalStateException("Topic '%s' (%s) has %d unique questions; %d required"
                        .formatted(test.getTopic(), test.getDifficulty(), test.getQuestions().size(), QUESTIONS_PER_TEST));
            }
        });

        Map<String, Set<QuestionDifficulty>> levelsByTopic = new LinkedHashMap<>();
        tests.forEach(test -> levelsByTopic
                .computeIfAbsent(test.getTopic(), ignored -> new LinkedHashSet<>())
                .add(test.getDifficulty()));
        levelsByTopic.forEach((topic, levels) -> {
            if (!levels.equals(Set.of(QuestionDifficulty.EASY, QuestionDifficulty.MEDIUM, QuestionDifficulty.HARD))) {
                throw new IllegalStateException("Topic '%s' must contain EASY, MEDIUM and HARD tests".formatted(topic));
            }
        });
        return tests;
    }

    private List<Question> uniqueQuestions(List<Question> questions, int limit) {
        Map<String, Question> unique = new LinkedHashMap<>();
        questions.forEach(question -> unique.putIfAbsent(semanticKey(question), question));
        return unique.values().stream().limit(limit).toList();
    }

    private String semanticKey(Question question) {
        String value = question.getType() == QuestionType.SENTENCE_CONSTRUCTION
                ? String.join(" ", question.getCorrectAnswers())
                : question.getQuestionText().replaceFirst("_+", question.getCorrectAnswers().get(0));
        return value.toLowerCase().replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    private record SeedQuestion(
            String topic,
            QuestionDifficulty difficulty,
            QuestionType type,
            String questionText,
            List<String> options,
            List<String> correctAnswers,
            String audioUrl
    ) {
    }

    private record QuestionKey(
            String topic,
            QuestionDifficulty difficulty,
            QuestionType type,
            String questionText,
            List<String> options,
            List<String> correctAnswers,
            String audioUrl
    ) {
        private static QuestionKey from(Question question) {
            return new QuestionKey(
                    question.getTopic(),
                    question.getDifficulty(),
                    question.getType(),
                    question.getQuestionText(),
                    question.getOptions(),
                    question.getCorrectAnswers(),
                    question.getAudioUrl()
            );
        }
    }

    private record SentenceKey(
            String topic,
            QuestionDifficulty difficulty,
            QuestionType type,
            String normalizedAnswer
    ) {
        private static SentenceKey from(Question question) {
            return new SentenceKey(
                    question.getTopic(),
                    question.getDifficulty(),
                    question.getType(),
                    String.join(" ", question.getCorrectAnswers())
                            .toLowerCase()
                            .replaceAll("[^\\p{L}\\p{N}]+", " ")
                            .trim()
            );
        }
    }

    private record TestKey(String topic, QuestionDifficulty difficulty) {
    }
}
