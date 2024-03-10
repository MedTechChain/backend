package nl.tudelft.medtechchain.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.Getter;
import nl.tudelft.medtechchain.model.UserRole;
import nl.tudelft.medtechchain.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


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
     * Gets the JWT from the HTTP request, validates and parses it, and extracts the JWT claims.
     * The JWT is assumed to be in the "Authorization" header and start with "Bearer ".
     *
     * @param request           the HTTP request with the JWT (aka the 'Bearer token')
     * @return                  JWT claims with the user details (userID and role) and JWT details
     */
    public Jws<Claims> resolveJwtClaims(HttpServletRequest request) {
        Optional<String> resolvedJwt = this.getJwtFromHeader(request);
        if (resolvedJwt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT is missing");
        }
        String jwt = resolvedJwt.get();
        Optional<Jws<Claims>> claims = this.validateAndParseClaims(jwt);
        if (claims.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT invalid or expired");
        }
        return claims.get();
    }

    /**
     * Checks if the JWT is valid and has not expired, and extracts the JWT claims (with some data).
     *
     * @param token                     the JWT token, as String without "Bearer " prefix
     * @return                          the JWT claims if the token is valid and has not expired yet
     */
    public Optional<Jws<Claims>> validateAndParseClaims(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(this.jwtSecretKey)
                    .build().parseSignedClaims(token);
            // Check if the fields are valid (userID and role)
            UUID userId = this.getUserId(claims);
            if (this.getRole(claims).equals(UserRole.UNKNOWN)) {
                return Optional.empty();
            }
            // Check if the user with the given userID exists
            this.authenticationService.loadUserByUserId(userId);
            // Check if the JWT has expired
            if (this.isExpired(claims)) {
                return Optional.empty();
            }
            // Since the JWT is valid, return the claims
            return Optional.of(claims);
        } catch (Exception e) {
            return Optional.empty();
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
     *
     * @param claims                    the (extracted) claims from the JWT
     * @return                          the user role present in the payload of the JWT
     */
    public UserRole getRole(Jws<Claims> claims) {
        String role = claims.getPayload().get("role").toString().toUpperCase();
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            return UserRole.UNKNOWN;
        }
    }
}
