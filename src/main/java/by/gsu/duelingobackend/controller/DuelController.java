package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.request.DuelFinishRequest;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import by.gsu.duelingobackend.service.DuelService;
import by.gsu.duelingobackend.service.matchmaking.MatchmakingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DuelController {

    private final MatchmakingService matchmakingService;
    private final DuelService duelService;

    @MessageMapping("/matchmaking/join")
    public void joinMatchmakingQueue(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UserDetailsImpl userDetails = (UserDetailsImpl) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            log.info(userDetails.getUsername() + "in controller");
            matchmakingService.joinMatchmakingQueue(userDetails.getUser());
        }
    }

    @MessageMapping("/matchmaking/cancel")
    public void cancelMatchmaking(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UserDetailsImpl userDetails = (UserDetailsImpl) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            matchmakingService.cancelMatchmaking(userDetails.getUser().getId());
        }
    }

    @MessageMapping("/duel/finish")
    public void finishDuel(@Payload DuelFinishRequest request, Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UserDetailsImpl userDetails = (UserDetailsImpl)
                    ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            duelService.processDuelResults(
                    request.duelId(),
                    userDetails.getUser().getId(),
                    request.correctAnswers(),
                    request.timeSpent()
            );
        }
    }
}
