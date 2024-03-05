package nl.tudelft.medtechchain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * A test class for the Application.java main class.
 */
@SpringBootTest
@Import(TestConfig.class)
public class ApplicationTest {

    @Autowired
    JavaMailSender javaMailSenderMock;

    @Test
    public void testApplication() {
        Assertions
                .assertThatCode(() -> Application.main(new String[]{"MedTech Chain"}))
                .doesNotThrowAnyException();
    }
}