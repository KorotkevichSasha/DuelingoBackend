package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.RelationshipRequest;
import by.gsu.duelingobackend.exceptions.InvalidOperationException;
import by.gsu.duelingobackend.mapper.UserMapper;
import by.gsu.duelingobackend.mapper.UserRelationshipMapper;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.UserRelationship;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.model.enums.RelationshipStatus;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

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

    @Test
    void acceptedRequestCountsAsInviteOnlyForTheSender() {
        User sender = User.builder().id(UUID.randomUUID()).username("sender").build();
        User receiver = User.builder().id(UUID.randomUUID()).username("receiver").build();
        UserRelationship request = UserRelationship.builder()
                .id(UUID.randomUUID())
                .fromUser(sender)
                .toUser(receiver)
                .status(RelationshipStatus.FRIEND_REQUEST)
                .build();
        when(relationshipRepository.findById(request.getId())).thenReturn(java.util.Optional.of(request));
        when(relationshipRepository.save(request)).thenReturn(request);

        relationshipService.updateRelationshipStatus(receiver.getId(), request.getId(), "ACCEPT");

        verify(achievementService).updateProgress(sender.getId(), AchievementConditionType.FRIEND_INVITED, 1);
        verify(achievementService, never()).updateProgress(receiver.getId(), AchievementConditionType.FRIEND_INVITED, 1);
    }
}
