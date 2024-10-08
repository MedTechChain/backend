package nl.medtechchain.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.Getter;
import nl.medtechchain.models.UserRole;
import nl.medtechchain.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;


/**
 * A class that manages JWTs, i.e. generation, parsing and validation etc.
 */
@Component
public class JwtProvider {

    private final SecretKey jwtSecretKey;

    @Getter
    @Value("${jwt.expiration-time}")
    private long jwtExpirationTime;

    private final AuthenticationService authenticationService;

    /**
     * Creates a JwtProvider object with the specified parameters.
     *
     * @param authenticationService     the authentication service that interacts with the database
     * @param jwtSecretKey              the secret key to sign the token
     */
    public JwtProvider(AuthenticationService authenticationService,
                       @Qualifier("secretKey") SecretKey jwtSecretKey) {
        this.authenticationService = authenticationService;
        this.jwtSecretKey = jwtSecretKey;
    }

    /**
     * Creates a JWT for a user, based on their userID (UUID) and role.
     * The expiration time (in minutes) is defined in the application.properties configuration file.
     *
     * @param userId                    the userID (UUID) of the user who will get the token
     * @param role                      the role of the user who will get the token
     * @param issueDate                 the date when the token has been issued (the current date)
     * @return                          the generated JWT for the user
     */
    public String generateJwtToken(UUID userId, UserRole role, Date issueDate) {
        Claims claims = Jwts.claims().subject(userId.toString()).add("role", role).build();
        // Convert the token expiration time (in minutes) to milliseconds
        Date expirationDate = new Date(issueDate.getTime() + this.jwtExpirationTime * 60000);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(issueDate)
                .expiration(expirationDate)
                .signWith(this.jwtSecretKey)
                .compact();
    }

    /**
     * Checks whether the provided JWT has expired.
     *
     * @param claims                    the claims of JWT (containing some data)
     * @return                          true if the token is expired, false if it is not expired
     */
    public boolean isExpired(Jws<Claims> claims) {
        return claims.getPayload().getExpiration().before(new Date());
    }

    /**
     * Gets the JWT from the HTTP request header.
     * Note that the JWT is assumed to be in the "Authorization" header and start with "Bearer ".
     *
     * @param request                   the HTTP request with the JWT token
     * @return                          the actual JWT, (i.e. String without "Bearer "), if present
     */
    public Optional<String> getJwtFromHeader(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authenticationHeader == null || !authenticationHeader.startsWith("Bearer")) {
            return Optional.empty();
        }
        return Optional.of(authenticationHeader.substring("Bearer ".length()));
    }

    /**
     * Checks if the JWT is valid and has not expired, and extracts the JWT claims (with some data).
     *
     * @param token                     the JWT token, as String without "Bearer " prefix
     * @return                          the JWT claims if the token is valid and has not expired yet
     * @throws ExpiredJwtException      if the JWT has expired
     * @throws JwtException             if there is an error when parsing and/or validating the JWT
     */
    public Jws<Claims> parseClaims(String token) throws ExpiredJwtException, JwtException {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(this.jwtSecretKey)
                    .build().parseSignedClaims(token);
            // Check if the fields are valid (userID and role)
            UUID userId = this.getUserId(claims);
            this.getRole(claims);
            // Check if the user with the given userID exists
            this.authenticationService.loadUserByUserId(userId);
            // Since the JWT is valid, return the claims
            return claims;
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "JWT has expired");
        } catch (JwtException | IllegalArgumentException | EntityNotFoundException e) {
            // Make IllegalArgumentException appear as JwtException for better exception handling
            throw new JwtException("JWT is invalid");
        }
    }

    /**
     * Gets userID (UUID) from the JWT claims.
     *
     * @param claims                    the (extracted) claims from the JWT
     * @return                          the userID present in the subject of the payload of the JWT
     */
    public UUID getUserId(Jws<Claims> claims) {
        return UUID.fromString(claims.getPayload().getSubject());
    }

    /**
     * Gets the user role from the JWT claims.
     * If the role cannot be determined, "unknown" is returned to the caller method.
     * Note that this method assumes that the role is valid, i.e. "Admin" (=0) or RESEARCHER (=1),
     *  otherwise IllegalArgumentException will be thrown when creating the UserRole enum.
     *
     * @param claims                    the (extracted) claims from the JWT
     * @return                          the user role present in the payload of the JWT
     * @throws IllegalArgumentException if the role in the claims is invalid
     */
    public UserRole getRole(Jws<Claims> claims) throws IllegalArgumentException {
        String role = claims.getPayload().get("role").toString().toUpperCase();
        return UserRole.valueOf(role);
    }
}
