package by.gsu.duelingobackend.dto.response.admin.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListResponse {
    private List<AdminUserDTO> users;
    private long totalUsers;
    private int currentPage;
    private int totalPages;
} 