package by.gsu.duelingobackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_SECONDS = 60;
    private static final int MAX_TRACKED_CLIENTS = 10_000;
    private final ConcurrentHashMap<String, Window> clients = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public AuthRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !"POST".equals(request.getMethod())
                || !(path.equals("/auth/sign-in")
                || path.equals("/auth/sign-up")
                || path.equals("/auth/verify-email")
                || path.equals("/auth/resend-verification")
                || path.equals("/auth/password-reset/request")
                || path.equals("/auth/password-reset/confirm"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long currentWindow = Instant.now().getEpochSecond() / WINDOW_SECONDS;
        String key = request.getRemoteAddr();
        Window window = clients.compute(key, (ignored, existing) ->
                existing == null || existing.id != currentWindow
                        ? new Window(currentWindow)
                        : existing.incremented());

        if (clients.size() > MAX_TRACKED_CLIENTS) {
            clients.entrySet().removeIf(entry -> entry.getValue().id < currentWindow);
        }

        if (window.count.get() > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
            objectMapper.writeValue(response.getOutputStream(), Map.of(
                    "status", 429,
                    "message", "Too many authentication attempts",
                    "timestamp", System.currentTimeMillis()));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static final class Window {
        private final long id;
        private final AtomicInteger count;

        private Window(long id) {
            this.id = id;
            this.count = new AtomicInteger(1);
        }

        private Window incremented() {
            count.incrementAndGet();
            return this;
        }
    }
}
