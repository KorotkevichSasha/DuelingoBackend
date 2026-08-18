package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.EditProfileRequest;
import by.gsu.duelingobackend.dto.response.PaginationResponse;
import by.gsu.duelingobackend.dto.response.user.FriendResponse;
import by.gsu.duelingobackend.dto.response.user.UserProfileResponse;
import by.gsu.duelingobackend.exceptions.EntityAlreadyExistsException;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.exceptions.FileUploadException;
import by.gsu.duelingobackend.exceptions.InvalidOperationException;
import by.gsu.duelingobackend.mapper.UserMapper;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static by.gsu.duelingobackend.util.Constants.USER_ALREADY_EXISTS_BY_EMAIL_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.USER_ALREADY_EXISTS_BY_USERNAME_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.USER_NOT_FOUND_BY_ID_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.USER_NOT_FOUND_BY_USERNAME_ERR_MSG;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final LeaderboardService leaderboardService;

    @Transactional
    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new EntityAlreadyExistsException(String.format(USER_ALREADY_EXISTS_BY_USERNAME_ERR_MSG, user.getUsername()));
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EntityAlreadyExistsException(String.format(USER_ALREADY_EXISTS_BY_EMAIL_ERR_MSG, user.getEmail()));
        }

        User newUser = userRepository.save(user);
        leaderboardService.updateUserScore(user.getId(), user.getPoints());

        return newUser;
    }

    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_USERNAME_ERR_MSG, username)));
        return userMapper.toUserProfileResponse(user);
    }

    public UserProfileResponse getProfileById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID_ERR_MSG, userId)));
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional
    @CachePut(value = "users", key = "#username")
    public UserProfileResponse updateProfile(String username, EditProfileRequest editProfileRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_USERNAME_ERR_MSG, username)));

        user.setEmail(editProfileRequest.email());

        userRepository.save(user);
        return userMapper.toUserProfileResponse(user);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_USERNAME_ERR_MSG, username)));

    }

    @Transactional
    public void recordLogin(String username) {
        User user = getByUsername(username);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public PaginationResponse<FriendResponse> searchUsers(UUID currentUserId, String query, Pageable pageable) {
        Page<User> page = userRepository.findByIdNotAndUsernameContainingIgnoreCase(
                currentUserId,
                query.trim(),
                pageable
        );

        List<FriendResponse> userDtos = page.getContent().stream()
                .map(userMapper::toFriendResponse)
                .toList();

        return new PaginationResponse<>(
                userDtos,
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }

    @Transactional
    @CachePut(value = "users", key = "#username")
    public UserProfileResponse updateAvatar(String username, MultipartFile file) {
        User user = getByUsername(username);
        String filename = user.getId().toString();
        String avatarUrl;
        try {
            avatarUrl = fileStorageService.uploadFile(file, filename);
        } catch (IOException e) {
            throw new FileUploadException(e.getMessage());
        }
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional
    @CachePut(value = "users", key = "#username")
    public UserProfileResponse selectDefaultAvatar(String username, int index) {
        if (index < 1 || index > 10) {
            throw new InvalidOperationException("Default avatar index must be between 1 and 10");
        }
        User user = getByUsername(username);
        try {
            fileStorageService.deleteAvatar(user.getId());
        } catch (IOException exception) {
            log.debug("No custom avatar to delete for {}", user.getId());
        }
        user.setAvatarUrl("default:" + index);
        userRepository.save(user);
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteAccount(String username) {
        User user = getByUsername(username);
        UUID userId = user.getId();

        leaderboardService.removeUser(userId);
        userRepository.delete(user);

        try {
            fileStorageService.deleteAvatar(userId);
        } catch (IOException exception) {
            log.warn("Account {} was deleted, but its avatar could not be removed", userId, exception);
        }
    }
}
