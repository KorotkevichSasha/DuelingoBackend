package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.request.DuelFinishRequest;
import by.gsu.duelingobackend.dto.response.DuelInHistoryResponse;
import by.gsu.duelingobackend.dto.response.PaginationResponse;
import by.gsu.duelingobackend.dto.response.MatchmakingEstimateResponse;
import by.gsu.duelingobackend.service.matchmaking.MatchmakingService;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import by.gsu.duelingobackend.service.DuelService;
import by.gsu.duelingobackend.service.DuelChallengeService;
import by.gsu.duelingobackend.dto.event.DuelChallengeEvent;
import by.gsu.duelingobackend.dto.event.DuelFoundEvent;
import by.gsu.duelingobackend.dto.request.DuelChallengeRequest;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/duels")
public class DuelRestController {

    private final DuelService duelService;
    private final MatchmakingService matchmakingService;
    private final DuelChallengeService duelChallengeService;

    @GetMapping("/matchmaking/estimate")
    public MatchmakingEstimateResponse getMatchmakingEstimate(
            @RequestParam(defaultValue = "MEDIUM") String difficulty) {
        return matchmakingService.getEstimate(difficulty);
    }

    @GetMapping("/history")
    public PaginationResponse<DuelInHistoryResponse> getUserDuelHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return duelService.getUserDuelHistory(userDetails.getUser().getId(), page, size);
    }

    @PostMapping("/finish")
    public void finishDuel(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody DuelFinishRequest request
    ) {
        duelService.processDuelResults(
                request.duelId(),
                userDetails.getUser().getId(),
                request.correctAnswers(),
                request.timeSpent(),
                request.answers()
        );
    }

    @PostMapping("/{duelId}/forfeit")
    public void forfeitDuel(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID duelId
    ) {
        duelService.forfeitDuel(duelId, userDetails.getUser().getId());
    }

    @PostMapping("/challenges")
    public DuelChallengeEvent challengeFriend(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody DuelChallengeRequest request) {
        return duelChallengeService.create(userDetails.getUser().getId(), request);
    }

    @GetMapping("/challenges/pending")
    public List<DuelChallengeEvent> pendingChallenges(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return duelChallengeService.pending(userDetails.getUser().getId());
    }

    @PostMapping("/challenges/{challengeId}/respond")
    public DuelFoundEvent respondToChallenge(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID challengeId, @RequestParam boolean accept) {
        return duelChallengeService.respond(userDetails.getUser().getId(), challengeId, accept);
    }
}
