package woodwork.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendWelcomeEmail_ShouldConstructAndSendCorrectMessage() {
        String testEmail = "newuser@woodwork.com";
        String testName = "Ron";

        emailService.sendWelcomeEmail(testEmail, testName);

        // the exact message that was handed to the mailSender
        // arguments captor let's us intercept the message before it is sent to mail trap
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        // verify the exacts contents
        assertEquals("noreply@woodwork.com", capturedMessage.getFrom());
        assertEquals(testEmail, Objects.requireNonNull(capturedMessage.getTo())[0]);
        assertEquals("Welcome to Woodwork, Ron!", capturedMessage.getSubject());
        assertTrue(Objects.requireNonNull(capturedMessage.getText()).contains("Hello Ron"));
    }
}
