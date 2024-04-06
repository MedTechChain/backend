package nl.medtechchain.jwt;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * A configuration class for creating the JWT key.
 */
@Configuration
public class JwtSecretKey {

    @Value("${jwt.secret-key-string}")
    private String secretKeyString;

    private final String secretKeyAlgorithm = "HmacSHA256";  // HMAC using SHA-256

    /**
     * Creates the secret key bean for a JWT.
     * By default, the secret key algorithm is HMAC using SHA-256.
     *
     * @return      the secret key for JWT
     */
    @Bean("secretKey")
    public SecretKey secretKey() {
        return new SecretKeySpec(
                this.secretKeyString.getBytes(StandardCharsets.UTF_8),
                this.secretKeyAlgorithm
        );
    }
}
