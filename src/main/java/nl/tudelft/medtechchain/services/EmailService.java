package nl.tudelft.medtechchain.services;

import nl.tudelft.medtechchain.models.email.EmailData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

/**
 * A service class used to send emails.
 * E.g. when registering a user, their credentials are sent to them by email.
 */
@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    /**
     * Creates an EmailService object. The sender email address is defined by the `from` field.
     *
     * @param mailSender        mail sender that is used to send emails
     * @param templateEngine    template engine to inject fields into Thymeleaf HTML templates
     */
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Sends an email to the specified email address, with the specified subject and content
     *  (created by Thymeleaf based on the HTML template).
     *
     * @param emailData         the data relevant for the email (subject, recipient, content etc.)
     */
    @Async
    public void sendEmail(EmailData emailData) {
        MimeMessagePreparator messagePreparation = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(from);
            messageHelper.setTo(emailData.getTo());
            messageHelper.setSubject(emailData.getSubject());
            String text = templateEngine.process(emailData.getTemplate(), emailData.getContext());
            messageHelper.setText(text, true);
        };
        this.mailSender.send(messagePreparation);
    }
}