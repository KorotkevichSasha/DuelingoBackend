package by.gsu.duelingobackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailApiEmailClient {

    private final ObjectMapper objectMapper;
    private final Object tokenLock = new Object();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.mail.gmail-api.client-id:}")
    private String clientId;

    @Value("${app.mail.gmail-api.client-secret:}")
    private String clientSecret;

    @Value("${app.mail.gmail-api.refresh-token:}")
    private String refreshToken;

    @Value("${app.mail.gmail-api.sender:${app.mail.from:${spring.mail.username}}}")
    private String senderEmail;

    @Value("${app.mail.gmail-api.token-url:https://oauth2.googleapis.com/token}")
    private String tokenUrl;

    @Value("${app.mail.gmail-api.base-url:https://gmail.googleapis.com}")
    private String baseUrl;

    private volatile CachedToken cachedToken;

    public boolean isConfigured() {
        return isPresent(clientId) && isPresent(clientSecret)
                && isPresent(refreshToken) && isPresent(senderEmail);
    }

    public boolean send(String to, String subject, String text) {
        if (!isConfigured()) return false;

        try {
            String rawMessage = buildRawMessage(to, subject, text);
            if (sendWithToken(rawMessage, getAccessToken())) return true;

            invalidateToken();
            return sendWithToken(rawMessage, getAccessToken());
        } catch (Exception exception) {
            log.error("Could not send an email to {} through Gmail API: {}", to, exception.getMessage());
            return false;
        }
    }

    private boolean sendWithToken(String rawMessage, String accessToken) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of("raw", rawMessage));
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/gmail/v1/users/me/messages/send"))
                .timeout(Duration.ofSeconds(25))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() >= 200 && response.statusCode() < 300) return true;
        if (response.statusCode() == 401) return false;
        throw new IllegalStateException("Gmail API returned HTTP " + response.statusCode());
    }

    private String getAccessToken() throws Exception {
        CachedToken current = cachedToken;
        if (current != null && current.valid()) return current.value();

        synchronized (tokenLock) {
            current = cachedToken;
            if (current != null && current.valid()) return current.value();

            String form = "client_id=" + encode(clientId)
                    + "&client_secret=" + encode(clientSecret)
                    + "&refresh_token=" + encode(refreshToken)
                    + "&grant_type=refresh_token";
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Google OAuth returned HTTP " + response.statusCode());
            }

            var json = objectMapper.readTree(response.body());
            String value = json.path("access_token").asText();
            long expiresIn = json.path("expires_in").asLong(3600);
            if (value.isBlank()) throw new IllegalStateException("Google OAuth response has no access token");

            cachedToken = new CachedToken(value, Instant.now().plusSeconds(Math.max(60, expiresIn - 60)));
            return value;
        }
    }

    private String buildRawMessage(String to, String subject, String text) throws Exception {
        var message = new MimeMessage(Session.getInstance(new Properties()));
        message.setFrom(new InternetAddress(senderEmail, "DuelRush", StandardCharsets.UTF_8.name()));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setText(text, StandardCharsets.UTF_8.name());
        message.saveChanges();

        var output = new ByteArrayOutputStream();
        message.writeTo(output);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(output.toByteArray());
    }

    private void invalidateToken() {
        synchronized (tokenLock) {
            cachedToken = null;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private record CachedToken(String value, Instant expiresAt) {
        boolean valid() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
