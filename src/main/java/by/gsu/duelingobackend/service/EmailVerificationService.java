package by.gsu.duelingobackend.service;

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
public class EmailVerificationService {
    private static final String CODE_PREFIX = "auth:email-code:";
    private static final String ATTEMPTS_PREFIX = "auth:email-attempts:";
    private static final String COOLDOWN_PREFIX = "auth:email-cooldown:";
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    private final StringRedisTemplate redis;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.email-verification.delivery-enabled:true}")
    private boolean deliveryEnabled;

    @Value("${app.email-verification.test-code:}")
    private String testCode;

    public boolean issueCode(User user, boolean enforceCooldown) {
        String email = normalize(user.getEmail());
        if (user.isEmailVerified()) return true;
        if (enforceCooldown && Boolean.TRUE.equals(redis.hasKey(COOLDOWN_PREFIX + email))) {
            throw new InvalidOperationException("A new verification code can be requested after one minute");
        }

        String code = testCode == null || testCode.isBlank()
                ? "%06d".formatted(random.nextInt(1_000_000))
                : testCode;
        redis.opsForValue().set(CODE_PREFIX + email, passwordEncoder.encode(code), CODE_TTL);
        redis.delete(ATTEMPTS_PREFIX + email);
        redis.opsForValue().set(COOLDOWN_PREFIX + email, "1", RESEND_COOLDOWN);
        return !deliveryEnabled || emailService.sendVerificationCode(email, code, CODE_TTL.toMinutes());
    }

    public boolean resend(String rawEmail) {
        User user = users.findByEmail(normalize(rawEmail))
                .filter(candidate -> !candidate.isEmailVerified())
                .orElseThrow(() -> new InvalidOperationException(
                        "No pending email verification was found"));
        return issueCode(user, true);
    }

    @Transactional
    public User verify(String rawEmail, String code) {
        String email = normalize(rawEmail);
        User user = users.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("The verification code is invalid or expired"));
        if (user.isEmailVerified()) {
            throw new InvalidOperationException("This email address is already verified");
        }

        Long attempts = redis.opsForValue().increment(ATTEMPTS_PREFIX + email);
        if (attempts != null && attempts == 1) {
            redis.expire(ATTEMPTS_PREFIX + email, CODE_TTL);
        }
        if (attempts != null && attempts > MAX_ATTEMPTS) {
            throw new InvalidOperationException("Too many verification attempts. Request a new code");
        }

        String encoded = redis.opsForValue().get(CODE_PREFIX + email);
        if (encoded == null || !passwordEncoder.matches(code, encoded)) {
            throw new InvalidOperationException("The verification code is invalid or expired");
        }

        user.setEmailVerified(true);
        users.save(user);
        redis.delete(CODE_PREFIX + email);
        redis.delete(ATTEMPTS_PREFIX + email);
        redis.delete(COOLDOWN_PREFIX + email);
        return user;
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
