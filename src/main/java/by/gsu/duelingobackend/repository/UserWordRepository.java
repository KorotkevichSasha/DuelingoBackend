package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.UserWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserWordRepository extends JpaRepository<UserWord, UUID> {

    List<UserWord> findByUserId(UUID userId);
    boolean existsByUserIdAndTerm(UUID userId, String term);
}
