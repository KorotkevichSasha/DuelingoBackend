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
    private final GmailApiEmailClient gmailApiEmailClient;
    private final BrevoEmailClient brevoEmailClient;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String verificationSender;

    @Async
    public void sendSimpleMessage(String to, String subject, String text) {
        if (gmailApiEmailClient.isConfigured()) {
            gmailApiEmailClient.send(to, subject, text);
            return;
        }
        if (brevoEmailClient.isConfigured()) {
            brevoEmailClient.send(to, subject, text);
            return;
        }
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
        String subject = "DuelRush — подтверждение почты";
        String text = "Ваш код подтверждения DuelRush: " + code
                + "\n\nКод действует " + validMinutes + " минут. "
                + "Если вы не регистрировались, просто проигнорируйте это письмо.";
        if (gmailApiEmailClient.isConfigured()) {
            boolean sent = gmailApiEmailClient.send(to, subject, text);
            if (sent) log.info("Verification email accepted by Gmail API for {}", to);
            return sent;
        } else if (brevoEmailClient.isConfigured()) {
            boolean sent = brevoEmailClient.send(to, subject, text);
            if (sent) log.info("Verification email accepted by Brevo for {}", to);
            return sent;
        }
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(verificationSender, "DuelRush", StandardCharsets.UTF_8.name()));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Verification email accepted by SMTP for {}", to);
            return true;
        } catch (Exception exception) {
            log.error("Could not send verification email to {}: {}", to, exception.getMessage());
            return false;
        }
    }

    @Async
    public void sendPasswordResetCode(String to, String code, long validMinutes) {
        String subject = "DuelRush — восстановление пароля";
        String text = "Ваш код для восстановления пароля DuelRush: " + code
                + "\n\nКод действует " + validMinutes + " минут. "
                + "Если вы не запрашивали восстановление, проигнорируйте письмо и никому не сообщайте код.";
        if (gmailApiEmailClient.isConfigured()) {
            gmailApiEmailClient.send(to, subject, text);
            return;
        } else if (brevoEmailClient.isConfigured()) {
            brevoEmailClient.send(to, subject, text);
            return;
        }
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(verificationSender, "DuelRush", StandardCharsets.UTF_8.name()));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("Password reset email accepted by SMTP for {}", to);
        } catch (Exception exception) {
            log.error("Could not send password reset email to {}: {}", to, exception.getMessage());
        }
    }
}
