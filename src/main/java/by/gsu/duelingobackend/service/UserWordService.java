package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.AddWordRequest;
import by.gsu.duelingobackend.dto.response.WordProgressResponse;
import by.gsu.duelingobackend.exceptions.EntityAlreadyExistsException;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.UserWord;
import by.gsu.duelingobackend.model.UserWordProgress;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.repository.UserWordProgressRepository;
import by.gsu.duelingobackend.repository.UserWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static by.gsu.duelingobackend.util.Constants.USER_ALREADY_ADDED_WORD_ERR_MSG;

@Service
@RequiredArgsConstructor
public class UserWordService {

    private final UserWordRepository userWordRepository;
    private final UserWordProgressRepository progressRepository;
    private final AchievementService achievementService;

    @Transactional
    public void addWord(User user, AddWordRequest request) {

        if (userWordRepository.existsByUserIdAndTerm(user.getId(), request.term())) {
            throw new EntityAlreadyExistsException(String.format(USER_ALREADY_ADDED_WORD_ERR_MSG, request.term()));
        }

        UserWord newWord = UserWord.builder()
                .user(user)
                .term(request.term())
                .translation(request.translation())
                .build();

        UserWord savedWord = userWordRepository.save(newWord);

        UserWordProgress progress = UserWordProgress.builder()
                .userId(user.getId())
                .wordId(savedWord.getId())
                .build();

        progressRepository.save(progress);
        achievementService.updateProgress(user.getId(), AchievementConditionType.WORD_ADDED, 1);
    }

    @Transactional(readOnly = true)
    public List<WordProgressResponse> getAllWords(UUID userId) {
        return progressRepository.findByUserId(userId).stream()
                .map(progress -> {
                    UserWord word = userWordRepository.findById(progress.getWordId())
                            .filter(item -> item.getUser().getId().equals(userId))
                            .orElseThrow(() -> new EntityNotFoundException("Word not found"));
                    return new WordProgressResponse(
                            userId,
                            word.getId(),
                            word.getTerm(),
                            word.getTranslation(),
                            progress.getRepetitions(),
                            progress.getEasinessFactor(),
                            progress.getNextReviewDate()
                    );
                })
                .sorted(Comparator.comparing(WordProgressResponse::term, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public void deleteWord(UUID userId, UUID wordId) {
        UserWord word = userWordRepository.findById(wordId)
                .filter(item -> item.getUser().getId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Word not found"));
        progressRepository.deleteByUserIdAndWordId(userId, wordId);
        userWordRepository.delete(word);
    }
}
