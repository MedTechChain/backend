package nl.tudelft.healthblocks.config;

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

    private final int passwordEncoderStrength = 12;

    /**
     * Instantiate a bean password encoder object which uses BCrypt to hash passwords.
     *
     * @return the BCrypt password encoder object
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(this.passwordEncoderStrength);
    }
}
