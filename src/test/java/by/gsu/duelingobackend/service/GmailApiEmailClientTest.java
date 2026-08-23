package by.gsu.duelingobackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GmailApiEmailClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicInteger tokenRequests = new AtomicInteger();
    private final AtomicInteger sendRequests = new AtomicInteger();
    private final AtomicReference<String> rawMessage = new AtomicReference<>();
    private HttpServer server;
    private GmailApiEmailClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/token", exchange -> {
            tokenRequests.incrementAndGet();
            respond(exchange, 200, "{\"access_token\":\"access-123\",\"expires_in\":3600}");
        });
        server.createContext("/gmail/v1/users/me/messages/send", exchange -> {
            assertThat(exchange.getRequestHeaders().getFirst("Authorization"))
                    .isEqualTo("Bearer access-123");
            var body = objectMapper.readTree(exchange.getRequestBody()).path("raw").asText();
            rawMessage.set(new String(Base64.getUrlDecoder().decode(body), StandardCharsets.UTF_8));
            sendRequests.incrementAndGet();
            respond(exchange, 200, "{}");
        });
        server.start();

        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        client = new GmailApiEmailClient(objectMapper);
        ReflectionTestUtils.setField(client, "clientId", "client-id");
        ReflectionTestUtils.setField(client, "clientSecret", "client-secret");
        ReflectionTestUtils.setField(client, "refreshToken", "refresh-token");
        ReflectionTestUtils.setField(client, "senderEmail", "duelrush.app@gmail.com");
        ReflectionTestUtils.setField(client, "tokenUrl", baseUrl + "/token");
        ReflectionTestUtils.setField(client, "baseUrl", baseUrl);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsMimeMessagesAndReusesTheAccessToken() {
        assertThat(client.isConfigured()).isTrue();
        assertThat(client.send("player@example.com", "DuelRush code", "Code: 123456")).isTrue();
        assertThat(client.send("player@example.com", "DuelRush code", "Code: 654321")).isTrue();

        assertThat(tokenRequests).hasValue(1);
        assertThat(sendRequests).hasValue(2);
        assertThat(rawMessage.get())
                .contains("From: DuelRush <duelrush.app@gmail.com>")
                .contains("To: player@example.com")
                .contains("Code: 654321");
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
