package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.request.RelationshipRequest;
import by.gsu.duelingobackend.dto.response.RelationshipResponse;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import by.gsu.duelingobackend.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/relationships")
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping("/friend-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RelationshipResponse sendFriendRequest(
            @RequestBody RelationshipRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return relationshipService.sendFriendRequest(principal.getUser().getId(), request);
    }

    @GetMapping("/friend-requests/incoming")
    public List<RelationshipResponse> getIncomingRequests(@AuthenticationPrincipal UserDetailsImpl principal) {
        return relationshipService.getIncomingRequests(principal.getUser().getId());
    }

    @PatchMapping("/friend-requests/{requestId}")
    public RelationshipResponse updateRelationshipStatus(
            @PathVariable UUID requestId,
            @RequestParam String action
    ) {
        return relationshipService.updateRelationshipStatus(requestId, action);
    }

    @PostMapping("/blocks")
    public RelationshipResponse blockUser(
            @RequestBody RelationshipRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return relationshipService.blockUser(principal.getUser().getId(), request);
    }

    @DeleteMapping("/blocks/{blockedUserId}")
    public void unblockUser(@PathVariable UUID blockedUserId) {
        relationshipService.unblockUser(blockedUserId);
    }
}
