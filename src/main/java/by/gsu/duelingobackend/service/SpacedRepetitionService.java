package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.model.UserWordProgress;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class SpacedRepetitionService {

    private static final double MIN_EASINESS_FACTOR = 1.3;

    public UserWordProgress calculateNextReview(UserWordProgress progress, int quality) {
        validateQuality(quality);

        double newEF = calculateNewEasinessFactor(progress.getEasinessFactor(), quality);
        int newInterval = calculateInterval(progress.getRepetitions(), newEF, quality);

        return updateProgress(progress, newEF, newInterval, quality);
    }

    private void validateQuality(int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }
    }

    private double calculateNewEasinessFactor(double currentEF, int quality) {
        double calculatedEF = currentEF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        return Math.max(calculatedEF, MIN_EASINESS_FACTOR);
    }

    private int calculateInterval(int repetitions, double ef, int quality) {
        if (quality < 3) {
            return 1;
        }
        return switch (repetitions + 1) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 7;
            default -> (int) Math.ceil((repetitions + 1) * ef);
        };
    }

    private UserWordProgress updateProgress(UserWordProgress progress, double newEF, int newInterval, int quality) {
        progress.setEasinessFactor(newEF);
        progress.setIntervalDays(newInterval);
        progress.setNextReviewDate(LocalDate.now().plusDays(newInterval));
        progress.setLastReviewed(LocalDateTime.now());

        if (quality < 3) {
            progress.setRepetitions(0);
        } else {
            progress.setRepetitions(progress.getRepetitions() + 1);
        }

        return progress;
    }
}
