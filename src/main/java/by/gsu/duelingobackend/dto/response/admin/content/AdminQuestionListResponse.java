package by.gsu.duelingobackend.dto.response.admin.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminQuestionListResponse {
    private List<AdminQuestionResponse> questions;
    private long totalQuestions;
    private int currentPage;
    private int totalPages;
} 