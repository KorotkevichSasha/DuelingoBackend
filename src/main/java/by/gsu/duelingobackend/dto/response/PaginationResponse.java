package by.gsu.duelingobackend.dto.response;

import java.util.List;

public record PaginationResponse<T>(
        List<T> content,
        Integer currentPage,
        Integer totalPages,
        Long totalItems
) {
}
