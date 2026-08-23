package by.gsu.duelingobackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrevoEmailClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.mail.brevo-api-key:}")
    private String apiKey;

    @Value("${app.mail.brevo-base-url:https://api.brevo.com}")
    private String baseUrl;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String senderEmail;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public boolean send(String to, String subject, String text) {
        if (!isConfigured()) return false;

        try {
            var payload = Map.of(
                    "sender", Map.of("name", "DuelRush", "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", text
            );
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(25))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 200 && response.statusCode() < 300) return true;

            log.error("Brevo rejected an email to {} with HTTP {}", to, response.statusCode());
        } catch (Exception exception) {
            log.error("Could not send an email to {} through Brevo: {}", to, exception.getMessage());
        }
        return false;
    }
}
