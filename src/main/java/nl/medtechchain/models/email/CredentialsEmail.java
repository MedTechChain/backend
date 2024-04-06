package nl.medtechchain.models.email;

import lombok.Getter;

/**
 * A class that stores the data necessary to send an email with the credentials.
 * It is used when registering a new user: upon successful registration, the credentials are sent to
 *  the new user by email.
 */
@Getter
public class CredentialsEmail extends EmailData {

    /**
     * Creates a CredentialsEmail object with the required data to send the credentials to the user.
     *
     * @param to            the recipient of the email
     * @param subject       the subject of the email (preferably one line)
     * @param name          the name of the new user (both first and last names)
     * @param username      the (generated) username of the new user
     * @param password      the (generated) password of the new user
     */
    public CredentialsEmail(String to, String subject,
                            String name, String username, String password) {
        super(to, subject, "credentials-email");
        this.context.setVariable("name", name);
        this.context.setVariable("username", username);
        this.context.setVariable("password", password);
    }
}
