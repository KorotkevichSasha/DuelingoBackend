package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.WordProgressResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.model.UserWord;
import by.gsu.duelingobackend.model.UserWordProgress;
import by.gsu.duelingobackend.repository.UserWordProgressRepository;
import by.gsu.duelingobackend.repository.UserWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static by.gsu.duelingobackend.util.Constants.WORD_NOT_FOUND_BY_ID_ERR_MSG;

@Service
@RequiredArgsConstructor
public class UserWordProgressService {

    private final UserWordProgressRepository progressRepository;
    private final UserWordRepository wordRepository;
    private final SpacedRepetitionService spacedRepetitionService;

    public List<WordProgressResponse> getDueWords(UUID userId) {
        List<UserWordProgress> dueProgresses = progressRepository
                .findByUserIdAndNextReviewDateLessThanEqual(userId, LocalDate.now());

        return dueProgresses.stream()
                .map(progress -> {
                    UserWord word = wordRepository.findById(progress.getWordId())
                            .orElseThrow(() -> new EntityNotFoundException(String.format(WORD_NOT_FOUND_BY_ID_ERR_MSG, progress.getWordId())));

                    return new WordProgressResponse(userId, word.getId(), word.getTerm(), word.getTranslation(), progress.getRepetitions(), progress.getEasinessFactor(), progress.getNextReviewDate());
                })
                .toList();
    }

    @Transactional
    public void processReview(UUID userId, UUID wordId, int quality) {
        UserWordProgress progress = progressRepository
                .findByUserIdAndWordId(userId, wordId)
                .orElseThrow(() -> new EntityNotFoundException("Progress not found"));

        UserWordProgress updatedProgress = spacedRepetitionService.calculateNextReview(progress, quality);
        progressRepository.save(updatedProgress);
    }
}
