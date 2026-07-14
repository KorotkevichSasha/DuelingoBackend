package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.response.AddWordRequest;
import by.gsu.duelingobackend.dto.response.WordProgressResponse;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import by.gsu.duelingobackend.service.UserWordProgressService;
import by.gsu.duelingobackend.service.UserWordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/words")
public class WordController {

    private final UserWordService userWordService;
    private final UserWordProgressService progressService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addWord(
            @RequestBody @Valid AddWordRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        userWordService.addWord(principal.getUser(), request);
    }

    @GetMapping("/due")
    public List<WordProgressResponse> getDueWords(@AuthenticationPrincipal UserDetailsImpl principal) {
        return progressService.getDueWords(principal.getUser().getId());
    }

    @PostMapping("/{wordId}/review")
    public void reviewWord(
            @PathVariable UUID wordId,
            @RequestParam @Min(0) @Max(5) int quality,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        progressService.processReview(principal.getUser().getId(), wordId, quality);
    }
}
