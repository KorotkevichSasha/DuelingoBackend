package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.TestDetailedResponse;
import by.gsu.duelingobackend.dto.response.TestSummaryResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.mapper.TestMapper;
import by.gsu.duelingobackend.model.UserTestProgress;
import by.gsu.duelingobackend.model.document.Test;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.model.enums.ProgressStatus;
import by.gsu.duelingobackend.repository.UserTestProgressRepository;
import by.gsu.duelingobackend.repository.test.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static by.gsu.duelingobackend.util.Constants.TEST_NOT_FOUND_BY_ID_ERR_MSG;


@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final UserTestProgressRepository userTestProgressRepository;
    private final TestMapper testMapper;
    private final AchievementService achievementService;

    public List<TestSummaryResponse> getTestsForTopic(String topic, UUID userId) {
        List<Test> tests = testRepository.findByTopicExcludingQuestions(topic);
        Set<String> completedTestIds = getCompletedTestIds(userId, tests);

        return tests.stream()
                .sorted(Comparator.comparing(Test::getDifficulty))
                .map(test -> testMapper.toSummaryResponse(
                        test,
                        completedTestIds.contains(test.getId().toString())
                ))
                .toList();
    }

    @Cacheable(value = "tests", key = "#id")
    public TestDetailedResponse getTestById(String id) {
        return testRepository.findById(id).map(testMapper::toDetailedResponse)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TEST_NOT_FOUND_BY_ID_ERR_MSG, id)));
    }

    @Cacheable(value = "tests", key = "'topics'")
    public List<String> getUniqueTopics() {
        return testRepository.findDistinctTopics().stream().filter(Objects::nonNull).toList();
    }

    @Transactional
    public void markTestAsPassed(String testId, UUID userId) {
        if (!testRepository.existsById(testId)) {
            throw new EntityNotFoundException(String.format(TEST_NOT_FOUND_BY_ID_ERR_MSG, testId));
        }

        Optional<UserTestProgress> existingProgress = userTestProgressRepository
                .findByUserIdAndTestId(userId, testId);

        UserTestProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            progress.setStatus(ProgressStatus.COMPLETED);
        } else {
            progress = UserTestProgress.builder()
                    .userId(userId)
                    .testId(testId)
                    .status(ProgressStatus.COMPLETED)
                    .build();
        }
        userTestProgressRepository.save(progress);
        achievementService.updateProgress(userId, AchievementConditionType.TEST_PASSED, 1);
    }


    private Set<String> getCompletedTestIds(UUID userId, List<Test> tests) {
        if (userId == null) return Set.of();

        List<String> testIds = tests.stream()
                .map(test -> test.getId().toString())
                .toList();

        return userTestProgressRepository
                .findByUserIdAndTestIdInAndStatus(userId, testIds, ProgressStatus.COMPLETED)
                .stream()
                .map(UserTestProgress::getTestId)
                .collect(Collectors.toSet());
    }
}