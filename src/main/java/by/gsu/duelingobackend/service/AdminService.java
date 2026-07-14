package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.admin.statistics.AdminContentStatisticsResponse;
import by.gsu.duelingobackend.dto.response.admin.statistics.AdminLearningStatisticsResponse;
import by.gsu.duelingobackend.dto.response.admin.statistics.AdminUserStatisticsResponse;
import by.gsu.duelingobackend.dto.response.admin.user.AdminUserDTO;
import by.gsu.duelingobackend.dto.response.admin.user.AdminUserListResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.Role;
import by.gsu.duelingobackend.repository.*;
import by.gsu.duelingobackend.repository.question.QuestionRepository;
import by.gsu.duelingobackend.repository.test.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserWordRepository userWordRepository;
    private final UserTestProgressRepository userTestProgressRepository;
    private final DuelRepository duelRepository;
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserListResponse getAllUsers(int page, int size, String search, Role role) {
        Page<User> userPage;
        PageRequest pageRequest = PageRequest.of(page, size);

        if (search != null && !search.isEmpty()) {
            userPage = userRepository.findByUsernameContainingOrEmailContaining(search, search, pageRequest);
        } else if (role != null) {
            userPage = userRepository.findByRole(role, pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        var users = userPage.getContent().stream()
                .map(this::mapToAdminUserDTO)
                .collect(Collectors.toList());

        return AdminUserListResponse.builder()
                .users(users)
                .totalUsers(userPage.getTotalElements())
                .currentPage(page)
                .totalPages(userPage.getTotalPages())
                .build();
    }

    @Transactional
    public void updateUserRole(UUID userId, Role newRole) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public void resetUserPassword(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Generate a random password or use a default one
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        // TODO: Send email with new password
    }

    public AdminUserStatisticsResponse getUserStatistics() {
        LocalDateTime now = LocalDateTime.now();
        return AdminUserStatisticsResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsersLast24h(userRepository.countByLastLoginAfter(now.minusDays(1)))
                .activeUsersLast7d(userRepository.countByLastLoginAfter(now.minusDays(7)))
                .activeUsersLast30d(userRepository.countByLastLoginAfter(now.minusDays(30)))
                // TODO: Add registration stats and users by role
                .build();
    }

    public AdminContentStatisticsResponse getContentStatistics() {
        return AdminContentStatisticsResponse.builder()
                .totalTests(testRepository.count())
                .totalQuestions(questionRepository.count())
                // TODO: Add detailed statistics
                .build();
    }

    public AdminLearningStatisticsResponse getLearningStatistics() {
        return AdminLearningStatisticsResponse.builder()
                .totalWordsLearned(userWordRepository.count())
                //.totalTestsCompleted(userTestProgressRepository.countByStatus(ProgressStatus.COMPLETED))
                .totalDuelsPlayed(duelRepository.count())
                // TODO: Add detailed statistics
                .build();
    }

    private AdminUserDTO mapToAdminUserDTO(User user) {
        return AdminUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .points(user.getPoints())
                .lastLogin(user.getLastLogin())
//                .totalWords(userWordRepository.countByUserId(user.getId()))
//                .completedTests(userTestProgressRepository.countByUserIdAndStatus(user.getId(), ProgressStatus.COMPLETED))
//                .totalDuels(duelRepository.countByPlayer1IdOrPlayer2Id(user.getId(), user.getId()))
                .build();
    }
} 