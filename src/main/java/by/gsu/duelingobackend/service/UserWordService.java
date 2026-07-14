package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.AddWordRequest;
import by.gsu.duelingobackend.exceptions.EntityAlreadyExistsException;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.UserWord;
import by.gsu.duelingobackend.model.UserWordProgress;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.repository.UserWordProgressRepository;
import by.gsu.duelingobackend.repository.UserWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
