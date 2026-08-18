package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> values;
    @Mock UserRepository users;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(values);
        service = new PasswordResetService(redis, users, passwordEncoder, emailService);
        ReflectionTestUtils.setField(service, "deliveryEnabled", true);
        ReflectionTestUtils.setField(service, "testCode", "123456");
    }

    @Test
    void requestDoesNotRevealWhetherAccountExists() {
        when(users.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        var response = service.request(" Unknown@Example.com ");

        assertThat(response.message()).isEqualTo(PasswordResetService.GENERIC_REQUEST_MESSAGE);
        verify(values).set(eq("auth:password-reset-cooldown:unknown@example.com"), eq("1"), any(Duration.class));
        verify(emailService, never()).sendPasswordResetCode(anyString(), anyString(), any(Long.class));
    }

    @Test
    void requestStoresOneTimeCodeAndSendsEmailForVerifiedAccount() {
        User user = User.builder().email("player@example.com").emailVerified(true).build();
        when(users.findByEmail("player@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("123456")).thenReturn("encoded-code");

        service.request("player@example.com");

        verify(values).set(eq("auth:password-reset-code:player@example.com"), eq("encoded-code"), any(Duration.class));
        verify(emailService).sendPasswordResetCode("player@example.com", "123456", 10);
    }

    @Test
    void validCodeChangesPasswordAndInvalidatesExistingTokens() {
        User user = User.builder()
                .email("player@example.com")
                .emailVerified(true)
                .password("old-hash")
                .tokenVersion(2)
                .build();
        when(values.increment("auth:password-reset-attempts:player@example.com")).thenReturn(1L);
        when(values.get("auth:password-reset-code:player@example.com")).thenReturn("encoded-code");
        when(users.findByEmail("player@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded-code")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword1", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword1")).thenReturn("new-hash");

        var response = service.confirm("player@example.com", "123456", "NewPassword1");

        assertThat(response.message()).isEqualTo(PasswordResetService.RESET_COMPLETE_MESSAGE);
        assertThat(user.getPassword()).isEqualTo("new-hash");
        assertThat(user.getTokenVersion()).isEqualTo(3);
        verify(users).save(user);
        verify(redis).delete("auth:password-reset-code:player@example.com");
        verify(redis).delete("auth:password-reset-attempts:player@example.com");
    }
}
