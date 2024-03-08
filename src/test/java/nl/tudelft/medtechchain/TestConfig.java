package nl.tudelft.medtechchain;

import org.hyperledger.fabric.client.Gateway;
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

    @Bean
    public Gateway gateway() {
        return Mockito.mock(Gateway.class);
    }
}