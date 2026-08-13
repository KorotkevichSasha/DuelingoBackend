package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.response.admin.statistics.AdminContentStatisticsResponse;
import by.gsu.duelingobackend.dto.response.admin.statistics.AdminLearningStatisticsResponse;
import by.gsu.duelingobackend.dto.response.admin.statistics.AdminUserStatisticsResponse;
import by.gsu.duelingobackend.dto.response.admin.user.AdminUserListResponse;
import by.gsu.duelingobackend.dto.response.admin.user.AdminUserReportResponse;
import by.gsu.duelingobackend.repository.UserReportRepository;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import by.gsu.duelingobackend.model.enums.Role;
import by.gsu.duelingobackend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserReportRepository userReportRepository;

    @GetMapping("/reports")
    public List<AdminUserReportResponse> getReports(
            @RequestParam(defaultValue = "100") int size) {
        return userReportRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, Math.min(size, 200)))
                .stream().map(report -> new AdminUserReportResponse(
                        report.getId(), report.getReporter().getId(), report.getReporter().getUsername(),
                        report.getReportedUser().getId(), report.getReportedUser().getUsername(),
                        report.getReason(), report.getCreatedAt())).toList();
    }

    @GetMapping("/users")
    public ResponseEntity<AdminUserListResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size, search, role));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable UUID userId,
            @RequestParam Role newRole) {
        adminService.updateUserRole(userId, newRole);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics/users")
    public ResponseEntity<AdminUserStatisticsResponse> getUserStatistics() {
        return ResponseEntity.ok(adminService.getUserStatistics());
    }

    @GetMapping("/statistics/content")
    public ResponseEntity<AdminContentStatisticsResponse> getContentStatistics() {
        return ResponseEntity.ok(adminService.getContentStatistics());
    }

    @GetMapping("/statistics/learning")
    public ResponseEntity<AdminLearningStatisticsResponse> getLearningStatistics() {
        return ResponseEntity.ok(adminService.getLearningStatistics());
    }
}
