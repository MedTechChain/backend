package nl.medtechchain.models.email;

import lombok.Getter;
import org.thymeleaf.context.Context;

/**
 * An abstract class that stores the basic email data (recipient, subject and template).
 * It is used to store the data common for all email types (i.e. child classes).
 */
@Getter
public abstract class EmailData {
    protected final String to;
    protected final String subject;
    protected final String template;
    protected final Context context = new Context();

    /**
     * Creates an EmailData object. Since EmailData class is abstract, this constructor is called by
     *  the child classes in their constructors.
     *
     * @param to            the recipient of the email
     * @param subject       the subject of the email (preferably one line)
     * @param template      the name of the template for the email (see resources/templates/)
     */
    public EmailData(String to, String subject, String template) {
        this.to = to;
        this.subject = subject;
        this.template = template;
    }
}
