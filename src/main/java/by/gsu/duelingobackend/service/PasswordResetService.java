package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.PasswordResetResponse;
import by.gsu.duelingobackend.exceptions.InvalidOperationException;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    static final String GENERIC_REQUEST_MESSAGE =
            "If an account exists for this email, a password reset code has been sent";
    static final String RESET_COMPLETE_MESSAGE = "Your password has been changed successfully";
    private static final String INVALID_CODE_MESSAGE = "The reset code is invalid or expired";
    private static final String CODE_PREFIX = "auth:password-reset-code:";
    private static final String ATTEMPTS_PREFIX = "auth:password-reset-attempts:";
    private static final String COOLDOWN_PREFIX = "auth:password-reset-cooldown:";
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration REQUEST_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    private final StringRedisTemplate redis;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.password-reset.delivery-enabled:true}")
    private boolean deliveryEnabled;

    @Value("${app.password-reset.test-code:}")
    private String testCode;

    public PasswordResetResponse request(String rawEmail) {
        String email = normalize(rawEmail);
        if (Boolean.TRUE.equals(redis.hasKey(COOLDOWN_PREFIX + email))) {
            return new PasswordResetResponse(GENERIC_REQUEST_MESSAGE);
        }

        redis.opsForValue().set(COOLDOWN_PREFIX + email, "1", REQUEST_COOLDOWN);
        users.findByEmail(email)
                .filter(User::isEmailVerified)
                .ifPresent(user -> issueCode(email));
        return new PasswordResetResponse(GENERIC_REQUEST_MESSAGE);
    }

    @Transactional
    public PasswordResetResponse confirm(String rawEmail, String code, String newPassword) {
        String email = normalize(rawEmail);
        Long attempts = redis.opsForValue().increment(ATTEMPTS_PREFIX + email);
        if (attempts != null && attempts == 1) {
            redis.expire(ATTEMPTS_PREFIX + email, CODE_TTL);
        }
        if (attempts != null && attempts > MAX_ATTEMPTS) {
            clearCode(email);
            throw new InvalidOperationException("Too many reset attempts. Request a new code");
        }

        String encodedCode = redis.opsForValue().get(CODE_PREFIX + email);
        User user = users.findByEmail(email).filter(User::isEmailVerified).orElse(null);
        if (user == null || encodedCode == null || !passwordEncoder.matches(code, encodedCode)) {
            throw new InvalidOperationException(INVALID_CODE_MESSAGE);
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new InvalidOperationException("The new password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        users.save(user);
        clearCode(email);
        redis.delete(COOLDOWN_PREFIX + email);
        return new PasswordResetResponse(RESET_COMPLETE_MESSAGE);
    }

    private void issueCode(String email) {
        String code = testCode == null || testCode.isBlank()
                ? "%06d".formatted(random.nextInt(1_000_000))
                : testCode;
        redis.opsForValue().set(CODE_PREFIX + email, passwordEncoder.encode(code), CODE_TTL);
        redis.delete(ATTEMPTS_PREFIX + email);
        if (deliveryEnabled) {
            emailService.sendPasswordResetCode(email, code, CODE_TTL.toMinutes());
        }
    }

    private void clearCode(String email) {
        redis.delete(CODE_PREFIX + email);
        redis.delete(ATTEMPTS_PREFIX + email);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
