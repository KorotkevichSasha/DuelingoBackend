package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.config.WebSocketConfig;
import by.gsu.duelingobackend.service.matchmaking.MatchmakingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringJUnitConfig(WebSocketConfig.class)
class DuelControllerIntegrationTest {

//    @Autowired
//    private WebSocketStompClient stompClient;

//    @MockBean
//    private MatchmakingService matchmakingService;
//
//    private StompSession stompSession;
//
//    @BeforeEach
//    void setup() throws Exception {
//        stompSession = stompClient
//                .connect("ws://localhost:{port}/ws", null)
//                .get();
//    }
//
//    @Test
//    @WithMockUser(username = "testUser", roles = "USER")
//    void joinMatchmaking_ValidRequest_AddsToQueue() throws Exception {
//        // Act
//        stompSession.send("/app/matchmaking/join", null);
//
//        // Assert
//        verify(matchmakingService, timeout(5000))
//                .joinMatchmakingQueue(any());
//    }
}