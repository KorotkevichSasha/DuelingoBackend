package by.gsu.duelingobackend.config;

import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.Role;
import by.gsu.duelingobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-bootstrap.username:}")
    private String username;

    @Value("${app.admin-bootstrap.email:}")
    private String email;

    @Value("${app.admin-bootstrap.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            return;
        }

        userRepository.findByUsername(username).ifPresentOrElse(existing -> {
            if (existing.getRole() != Role.ADMIN) {
                existing.setRole(Role.ADMIN);
                existing.setEmailVerified(true);
                userRepository.save(existing);
                log.info("Promoted configured operator account to ADMIN");
            }
        }, () -> {
            User admin = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(Role.ADMIN)
                    .emailVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("Created configured operator account");
        });
    }
}
