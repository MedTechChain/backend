package nl.tudelft.healthblocks.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * A configuration class for creating JWT key.
 */
@Configuration
public class JwtSecretKey {
    @Value("${jwt.secretkeystring}")
    private String secretKeyString;

    /**
     * Creates the secret key bean for a JWT.
     *
     * @return the secret key for JWT
     */
    @Bean("secretKey")
    public SecretKey secretKey() {
        // Algorithm is HMAC using SHA-256
        // TODO: maybe use Public Key Crypto?
        return new SecretKeySpec(this.secretKeyString.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
