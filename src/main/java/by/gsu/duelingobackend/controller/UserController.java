package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.request.EditProfileRequest;
import by.gsu.duelingobackend.dto.response.PaginationResponse;
import by.gsu.duelingobackend.dto.response.user.FriendResponse;
import by.gsu.duelingobackend.dto.response.user.UserProfileResponse;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import by.gsu.duelingobackend.service.FileStorageService;
import by.gsu.duelingobackend.service.RelationshipService;
import by.gsu.duelingobackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RelationshipService relationshipService;
    private final FileStorageService fileStorageService;

    @GetMapping("/profile")
    public UserProfileResponse getProfile(@AuthenticationPrincipal UserDetailsImpl principal) {
        return userService.getProfile(principal.getUsername());
    }

    @PostMapping("/profile/avatar")
    public UserProfileResponse uploadAvatar(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam("file") MultipartFile file) {
        return userService.updateAvatar(principal.getUsername(), file);
    }

    @GetMapping("/avatar/{userId}")
    public ResponseEntity<Resource> getAvatar(
            @PathVariable UUID userId,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) throws IOException {

        String currentETag = fileStorageService.calculateETag(userId);

        if (currentETag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(currentETag)
                    .build();
        }

        Resource resource = fileStorageService.loadFile(userId);
        String contentType = fileStorageService.getContentType(userId);
        return ResponseEntity.ok()
                .eTag(currentETag)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400, must-revalidate")
                .body(resource);
    }

    @PutMapping("/profile")
    public UserProfileResponse editProfile(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestBody @Valid EditProfileRequest editProfileRequest) {
        return userService.updateProfile(principal.getUsername(), editProfileRequest);
    }

    @GetMapping("/{userId}/profile")
    public UserProfileResponse getUserProfile(@PathVariable UUID userId) {
        return userService.getProfileById(userId);
    }

    @GetMapping("/search")
    public PaginationResponse<FriendResponse> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.searchUsers(query, PageRequest.of(page, size));
    }

    @GetMapping("/{userId}/friends")
    public List<FriendResponse> getFriends(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return relationshipService.getUserFriends(userId, PageRequest.of(page, size));
    }

    @GetMapping("/friends")
    public List<FriendResponse> getAuthenticatedUserFriends(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return relationshipService.getUserFriends(principal.getUser().getId(), PageRequest.of(page, size));
    }
}
