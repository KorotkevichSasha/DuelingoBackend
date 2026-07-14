package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.RelationshipRequest;
import by.gsu.duelingobackend.dto.response.RelationshipResponse;
import by.gsu.duelingobackend.dto.response.user.FriendResponse;
import by.gsu.duelingobackend.exceptions.EntityAlreadyExistsException;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.exceptions.InvalidOperationException;
import by.gsu.duelingobackend.mapper.UserMapper;
import by.gsu.duelingobackend.mapper.UserRelationshipMapper;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.UserRelationship;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.model.enums.RelationshipStatus;
import by.gsu.duelingobackend.repository.UserRelationshipRepository;
import by.gsu.duelingobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static by.gsu.duelingobackend.util.Constants.BLOCK_RELATIONSHIP_NOT_FOUND_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.FRIEND_REQUEST_ALREADY_EXISTS_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.ONLY_BLOCKED_RELATIONSHIPS_CAN_BE_UNBLOCKED_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.ONLY_PENDING_REQUESTS_CAN_BE_REJECTED_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.RELATIONSHIP_NOT_FOUND_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.SELF_BLOCK_NOT_ALLOWED_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.SELF_FRIEND_REQUEST_NOT_ALLOWED_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.USER_NOT_FOUND_BY_ID_ERR_MSG;

@Service
@Transactional
@RequiredArgsConstructor
public class RelationshipService {

    private final EmailService emailService;
    private final UserRelationshipRepository relationshipRepository;
    private final UserRepository userRepository;
    private final UserRelationshipMapper userRelationshipMapper;
    private final UserMapper userMapper;
    private final AchievementService achievementService;

    public RelationshipResponse sendFriendRequest(UUID fromUserId, RelationshipRequest request) {

        List<User> users = userRepository.findAllById(List.of(fromUserId, request.toUserId()));

        if (users.size() < 2) {
            throw new EntityNotFoundException(
                    String.format(USER_NOT_FOUND_BY_ID_ERR_MSG, fromUserId)
            );
        }

        User fromUser = users.get(0).getId().equals(fromUserId) ? users.get(0) : users.get(1);
        User toUser = users.get(0).getId().equals(request.toUserId()) ? users.get(0) : users.get(1);

        if (fromUser.equals(toUser)) {
            throw new InvalidOperationException(
                    String.format(SELF_FRIEND_REQUEST_NOT_ALLOWED_ERR_MSG, fromUserId)
            );
        }

        boolean relationshipExists = relationshipRepository.existsByUsers(fromUser, toUser);

        if (relationshipExists) {
            throw new EntityAlreadyExistsException(
                    String.format(FRIEND_REQUEST_ALREADY_EXISTS_ERR_MSG, fromUserId, request.toUserId())
            );
        }

        UserRelationship relationship = UserRelationship.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(RelationshipStatus.FRIEND_REQUEST)
                .build();

        UserRelationship savedRelationship = relationshipRepository.save(relationship);

        String message = String.format(
                "%s хочет добавить вас в друзья!",
                fromUser.getUsername()
        );

        emailService.sendSimpleMessage(
                toUser.getEmail(),
                "Новая заявка в друзья",
                message
        );

        return userRelationshipMapper.toResponse(savedRelationship);
    }

    public List<RelationshipResponse> getIncomingRequests(UUID userId) {
        return relationshipRepository.findByToUserIdAndStatus(userId, RelationshipStatus.FRIEND_REQUEST)
                .stream()
                .map(userRelationshipMapper::toResponse)
                .toList();
    }

    public RelationshipResponse updateRelationshipStatus(UUID requestId, String action) {
        UserRelationship relationship = relationshipRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(RELATIONSHIP_NOT_FOUND_ERR_MSG, requestId)));

        if (relationship.getStatus() != RelationshipStatus.FRIEND_REQUEST) {
            throw new InvalidOperationException(String.format(ONLY_PENDING_REQUESTS_CAN_BE_REJECTED_ERR_MSG, requestId));
        }

        switch (action.toUpperCase()) {
            case "ACCEPT" -> {
                relationship.setStatus(RelationshipStatus.FRIEND);
                UserRelationship updated = relationshipRepository.save(relationship);
                achievementService.updateProgress(relationship.getFromUser().getId(), AchievementConditionType.FRIEND_ADDED, 1);
                achievementService.updateProgress(relationship.getToUser().getId(), AchievementConditionType.FRIEND_ADDED, 1);
                return userRelationshipMapper.toResponse(updated);
            }
            case "REJECT" -> {
                relationshipRepository.delete(relationship);
                return null;
            }
            default -> throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

    public RelationshipResponse blockUser(UUID fromUserId, RelationshipRequest request) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID_ERR_MSG, fromUserId)));

        User toUser = userRepository.findById(request.toUserId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID_ERR_MSG, request.toUserId())));

        if (fromUser.equals(toUser)) {
            throw new InvalidOperationException(String.format(SELF_BLOCK_NOT_ALLOWED_ERR_MSG, fromUserId));
        }

        UserRelationship relationship = relationshipRepository
                .findByFromUserAndToUser(fromUser, toUser)
                .orElseGet(() -> UserRelationship.builder()
                        .fromUser(fromUser)
                        .toUser(toUser)
                        .build());

        relationship.setStatus(RelationshipStatus.BLOCK);
        UserRelationship savedRelationship = relationshipRepository.save(relationship);

        return userRelationshipMapper.toResponse(savedRelationship);
    }

    public void unblockUser(UUID blockId) {
        UserRelationship relationship = relationshipRepository.findById(blockId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(BLOCK_RELATIONSHIP_NOT_FOUND_ERR_MSG, blockId)));

        if (relationship.getStatus() != RelationshipStatus.BLOCK) {
            throw new InvalidOperationException(String.format(ONLY_BLOCKED_RELATIONSHIPS_CAN_BE_UNBLOCKED_ERR_MSG, blockId));
        }

        relationshipRepository.delete(relationship);
    }

    public List<FriendResponse> getUserFriends(UUID userId, Pageable pageable) {
        return relationshipRepository.findFriendsByUserId(userId, pageable)
                .stream()
                .map(rel -> rel.getFromUser().getId().equals(userId) ?
                        userMapper.toFriendResponse(rel.getToUser()) :
                        userMapper.toFriendResponse(rel.getFromUser())
                )
                .toList();
    }
}
