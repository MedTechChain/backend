package nl.tudelft.medtechchain.services;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * A service class used to send emails.
 * E.g. when registering a user, their credentials are sent to them by email.
 */
@Service
@AllArgsConstructor
public class EmailService {

    private JavaMailSender mailSender;

    /**
     * Sends an email to the specified email address, with the specified subject and content.
     *
     * @param to            the recipient of the email
     * @param subject       the subject of the email
     * @param text          the content of the email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply.healthblocks@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        this.mailSender.send(message);
    }
}
