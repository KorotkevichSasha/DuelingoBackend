package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.response.TestDetailedResponse;
import by.gsu.duelingobackend.dto.response.TestSummaryResponse;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import by.gsu.duelingobackend.service.TestService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tests")
public class TestController {

    private final TestService testService;

    @GetMapping
    public List<TestSummaryResponse> getTestsForTopic(
            @RequestParam @NotBlank String topic,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return testService.getTestsForTopic(topic, principal.getUser().getId());
    }

    @GetMapping("/{testId}")
    public TestDetailedResponse getTestById(@PathVariable @NotBlank String testId) {
        return testService.getTestById(testId);
    }

    @PostMapping("/{testId}/mark-as-passed")
    public void markTestAsPassed(@PathVariable @NotBlank String testId,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        testService.markTestAsPassed(testId, userDetails.getUser().getId());
    }

    @GetMapping("/topics")
    public List<String> getUniqueTestTopics() {
        return testService.getUniqueTopics();
    }
}