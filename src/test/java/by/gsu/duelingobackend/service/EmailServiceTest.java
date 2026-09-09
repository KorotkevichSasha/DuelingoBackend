package by.gsu.duelingobackend.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final GmailApiEmailClient gmail = mock(GmailApiEmailClient.class);
    private final BrevoEmailClient brevo = mock(BrevoEmailClient.class);
    private EmailService service;

    @BeforeEach
    void setUp() {
        service = new EmailService(mailSender, gmail, brevo);
        ReflectionTestUtils.setField(service, "sender", "duelrush.app@gmail.com");
        ReflectionTestUtils.setField(service, "verificationSender", "duelrush.app@gmail.com");
    }

    @Test
    void fallsBackToSmtpWhenConfiguredApiProvidersFail() {
        MimeMessage smtpMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(gmail.isConfigured()).thenReturn(true);
        when(gmail.send("player@example.com", "DuelRush — подтверждение почты",
                "Ваш код подтверждения DuelRush: 123456\n\nКод действует 10 минут. Если вы не регистрировались, просто проигнорируйте это письмо."))
                .thenReturn(false);
        when(brevo.isConfigured()).thenReturn(false);
        when(mailSender.createMimeMessage()).thenReturn(smtpMessage);

        assertThat(service.sendVerificationCode("player@example.com", "123456", 10)).isTrue();

        verify(mailSender).send(smtpMessage);
    }
}
