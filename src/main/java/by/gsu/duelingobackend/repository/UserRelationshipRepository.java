package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.UserRelationship;
import by.gsu.duelingobackend.model.enums.RelationshipStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRelationshipRepository extends JpaRepository<UserRelationship, UUID> {

    Optional<UserRelationship> findByFromUserAndToUser(User fromUser, User toUser);

    @Query("""
        SELECT COUNT(r) > 0 FROM UserRelationship r
        WHERE (r.fromUser = :fromUser AND r.toUser = :toUser) 
           OR (r.fromUser = :toUser AND r.toUser = :fromUser)
    """)
    boolean existsByUsers(@Param("fromUser") User fromUser, @Param("toUser") User toUser);


    List<UserRelationship> findByToUserIdAndStatus(UUID toUserId, RelationshipStatus status);

    @Query("""
        SELECT r FROM UserRelationship r 
        WHERE (r.fromUser.id = :userId OR r.toUser.id = :userId) 
        AND r.status = 'FRIEND'
    """)
    List<UserRelationship> findFriendsByUserId(@Param("userId") UUID userId, Pageable pageable);

}
