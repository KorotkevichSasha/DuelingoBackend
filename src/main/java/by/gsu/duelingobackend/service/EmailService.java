package by.gsu.duelingobackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String verificationSender;

    @Async
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Error while sending mail: {}", ex.getMessage());
        }
    }

    public boolean sendVerificationCode(String to, String code, long validMinutes) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(verificationSender, "DuelRush", StandardCharsets.UTF_8.name()));
            helper.setTo(to);
            helper.setSubject("DuelRush — подтверждение почты");
            helper.setText("Ваш код подтверждения DuelRush: " + code
                    + "\n\nКод действует " + validMinutes + " минут. "
                    + "Если вы не регистрировались, просто проигнорируйте это письмо.");
            mailSender.send(message);
            log.info("Verification email accepted by SMTP for {}", to);
            return true;
        } catch (Exception exception) {
            log.error("Could not send verification email to {}: {}", to, exception.getMessage());
            return false;
        }
    }
}
