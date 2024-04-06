package nl.tudelft.medtechchain;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;


@Configuration
public class TestConfig {

    /**
     * Creates a test mock for the JavaMailSender object.
     *
     * @return      the created mock of the JavaMailSender object
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

}