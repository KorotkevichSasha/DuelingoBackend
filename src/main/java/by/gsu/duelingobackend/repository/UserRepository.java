package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id IN :ids ORDER BY u.id")
    List<User> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u ORDER BY u.points DESC")
    Page<User> findUsersOrderByPointsDesc(Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.points > :points")
    Integer countUsersWithPointsGreaterThan(@Param("points") Integer points);

    Page<User> findByUsernameContainingIgnoreCase(String query, Pageable pageable);

    Page<User> findByIdNotAndUsernameContainingIgnoreCase(UUID excludedUserId, String query, Pageable pageable);

    // Admin functionality
    Page<User> findByUsernameContainingOrEmailContaining(String username, String email, Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    long countByLastLoginAfter(LocalDateTime date);

    long countByRole(Role role);
}
