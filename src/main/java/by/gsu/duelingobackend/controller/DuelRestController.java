package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.response.DuelInHistoryResponse;
import by.gsu.duelingobackend.dto.response.PaginationResponse;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import by.gsu.duelingobackend.service.DuelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/duels")
public class DuelRestController {

    private final DuelService duelService;

    @GetMapping("/history")
    public PaginationResponse<DuelInHistoryResponse> getUserDuelHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return duelService.getUserDuelHistory(userDetails.getUser().getId(), page, size);
    }
}
