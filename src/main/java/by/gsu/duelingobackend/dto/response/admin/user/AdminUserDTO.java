package by.gsu.duelingobackend.dto.response.admin.user;

import by.gsu.duelingobackend.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {
    private UUID id;
    private String username;
    private String email;
    private Role role;
    private Integer points;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private int totalWords;
    private int completedTests;
    private int totalDuels;
} 