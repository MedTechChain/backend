package nl.medtechchain;

import org.assertj.core.api.Assertions;
import org.hyperledger.fabric.client.Gateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

/**
 * A test class for the Application.java main class.
 */
@SpringBootTest
@ActiveProfiles({"test"})
public class ApplicationTest {

    @Autowired
    JavaMailSender javaMailSenderMock;

    @Autowired
    Gateway gatewayMock;

    @Test
    public void testApplication() {
        Assertions
                .assertThatCode(() ->
                        Application.main(new String[]{"--spring.profiles.active=test"}))
                .doesNotThrowAnyException();
    }
}