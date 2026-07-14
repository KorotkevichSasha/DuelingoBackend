package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.UserTestProgress;
import by.gsu.duelingobackend.model.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTestProgressRepository extends JpaRepository<UserTestProgress, UUID> {

    Optional<UserTestProgress> findByUserIdAndTestId(UUID userId, String testId);

    List<UserTestProgress> findByUserIdAndTestIdInAndStatus(UUID userId, Collection<String> testId, ProgressStatus status);
}
