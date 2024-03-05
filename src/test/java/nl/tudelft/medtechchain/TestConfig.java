package nl.tudelft.medtechchain;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
public class TestConfig {

    @Bean
    public JavaMailSender getJavaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
}