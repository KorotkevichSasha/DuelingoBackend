package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserReportRepository extends JpaRepository<UserReport, UUID> {
    boolean existsByReporterIdAndReportedUserIdAndCreatedAtAfter(
            UUID reporterId, UUID reportedUserId, LocalDateTime after);
    List<UserReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
