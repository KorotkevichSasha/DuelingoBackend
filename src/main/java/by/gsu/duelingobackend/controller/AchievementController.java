package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.response.UserAchievementResponse;
import by.gsu.duelingobackend.dto.response.AchievementClaimResponse;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import by.gsu.duelingobackend.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public List<UserAchievementResponse> getUserAchievements(@AuthenticationPrincipal UserDetailsImpl principal) {
        return achievementService.getUserAchievements(principal.getUser().getId());
    }

    @PostMapping("/{achievementId}/claim")
    public AchievementClaimResponse claimReward(
            @PathVariable UUID achievementId,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return achievementService.claimReward(principal.getUser().getId(), achievementId);
    }
}
