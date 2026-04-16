package woodwork.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@woodwork.com"); // MailTrap ignores this, but Spring requires it
        message.setTo(toEmail);
        message.setSubject("Welcome to Woodwork, " + username + "!");
        message.setText("Hello " + username + ",\n\n" +
                "Welcome to the Woodwork application! Your account has been successfully created.\n\n" +
                "Best regards,\nThe Woodwork Team");

        mailSender.send(message);
    }
}
