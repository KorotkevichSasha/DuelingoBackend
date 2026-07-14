package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.response.UserAchievementResponse;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import by.gsu.duelingobackend.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public List<UserAchievementResponse> getUserAchievements(@AuthenticationPrincipal UserDetailsImpl principal) {
        return achievementService.getUserAchievements(principal.getUser().getId());
    }
}
