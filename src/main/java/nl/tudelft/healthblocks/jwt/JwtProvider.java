package nl.tudelft.healthblocks.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import nl.tudelft.healthblocks.security.UserRole;
import nl.tudelft.healthblocks.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtProvider {

    private final SecretKey jwtSecretKey;

    @Value("${jwt.expirationtime}")
    private long jwtExpirationTime;

    private final AuthenticationService authenticationService;

    /**
     * Creates a JwtProvider object with the specified parameters.
     *
     * @param authenticationService     the authentication service that interacts with the user database
     * @param jwtSecretKey              the secret key to sign the token
     */
    public JwtProvider(AuthenticationService authenticationService, @Qualifier("secretKey") SecretKey jwtSecretKey) {
        this.authenticationService = authenticationService;
        this.jwtSecretKey = jwtSecretKey;
    }

    public String generateJwtToken(long userId, UserRole role, Date issueDate) {
        Claims claims = Jwts.claims().subject(String.valueOf(userId)).add("role", role).build();

        // Convert the token expiration time (in minutes) to milliseconds
        Date expirationDate = new Date(issueDate.getTime() + this.jwtExpirationTime * 60000);


        return Jwts.builder().claims(claims)
                .issuedAt(issueDate).expiration(expirationDate)
                .signWith(this.jwtSecretKey).compact();
    }

    private boolean isExpired(Jws<Claims> claims) {
        return claims.getPayload().getExpiration().before(new Date(System.currentTimeMillis()));
    }

    public Optional<Jws<Claims>> validateAndParseClaims(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(this.jwtSecretKey).build().parseSignedClaims(token);
            long userId = Long.parseLong(claims.getPayload().getSubject());
            this.authenticationService.loadUserByUserId(userId); // Check if the user with the given userID exists
            if (isExpired(claims)) return Optional.empty();

            return Optional.of(claims);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public long getUserId(Jws<Claims> claims) {
        return Long.parseLong(claims.getPayload().getSubject());
    }

    public String getRole(Jws<Claims> claims) {
        return claims.getPayload().get("role").toString();
    }
}
