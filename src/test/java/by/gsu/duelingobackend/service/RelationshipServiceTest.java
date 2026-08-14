package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.RelationshipRequest;
import by.gsu.duelingobackend.exceptions.InvalidOperationException;
import by.gsu.duelingobackend.mapper.UserMapper;
import by.gsu.duelingobackend.mapper.UserRelationshipMapper;
import by.gsu.duelingobackend.repository.UserRelationshipRepository;
import by.gsu.duelingobackend.repository.UserReportRepository;
import by.gsu.duelingobackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RelationshipServiceTest {

    @Mock EmailService emailService;
    @Mock UserRelationshipRepository relationshipRepository;
    @Mock UserRepository userRepository;
    @Mock UserRelationshipMapper userRelationshipMapper;
    @Mock UserMapper userMapper;
    @Mock AchievementService achievementService;
    @Mock UserReportRepository userReportRepository;

    @InjectMocks RelationshipService relationshipService;

    @Test
    void rejectsSelfFriendRequestBeforeReadingUsers() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> relationshipService.sendFriendRequest(
                userId,
                new RelationshipRequest(userId)
        )).isInstanceOf(InvalidOperationException.class)
          .hasMessageContaining("cannot send a friend request to themselves");

        verifyNoInteractions(userRepository, relationshipRepository, emailService);
    }
}
