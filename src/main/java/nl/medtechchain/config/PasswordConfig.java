package nl.medtechchain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * A configuration class for PasswordEncoder.
 * PasswordEncoder checks whether the hashes of the provided and actual passwords match.
 */
@Configuration
public class PasswordConfig {

    @Value("${password.encoder-strength}")
    private final int passwordEncoderStrength = 12;

    /**
     * Instantiates a bean password encoder object which uses BCrypt to hash passwords.
     *
     * @return          the BCrypt PasswordEncoder object
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(this.passwordEncoderStrength);
    }
}
