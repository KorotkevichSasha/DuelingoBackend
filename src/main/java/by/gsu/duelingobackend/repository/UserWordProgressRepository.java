package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, UUID> {

    Optional<UserWordProgress> findByUserIdAndWordId(UUID userId, UUID wordId);
    List<UserWordProgress> findByUserIdAndNextReviewDateLessThanEqual(UUID userId, LocalDate date);
}
