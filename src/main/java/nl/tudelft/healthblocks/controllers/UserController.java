package nl.tudelft.healthblocks.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import nl.tudelft.healthblocks.jwt.JwtProvider;
import nl.tudelft.healthblocks.model.Researcher;
import nl.tudelft.healthblocks.model.UserData;
import nl.tudelft.healthblocks.model.UserRole;
import nl.tudelft.healthblocks.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


/**
 * A (REST) controller class that provides API endpoints and interacts with the Service class.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationManager authenticationManager;

    private final AuthenticationService authenticationService;

    private final JwtProvider jwtProvider;

    private final ObjectMapper objectMapper;

    /**
     * Test endpoint. TODO: remove
     */
    @GetMapping
    public ResponseEntity<String> greetings() {
        return ResponseEntity.ok("Salut, Alin!");
    }

    /**
     * Logs in the user. If successful, sends back a JSON with JWT.
     * Both admin and researchers are allowed to perform this operation.
     *
     * @param request           the received HTTP request
     * @param response          the HTTP response with the JWT that will be sent back
     * @throws IOException      if something goes wrong during the JSON deserialization process
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();

        this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserData user = this.authenticationService.loadUserByUsername(username);
        String jwt = this.jwtProvider.generateJwtToken(
                user.getUserId(), user.getRole(), new Date()
        );

        String responseBody = this.objectMapper.createObjectNode()
                .put("jwt", jwt)
                .put("token_type", "JWT")
                .put("expires_in", this.jwtProvider.getJwtExpirationTime()).toString();
        response.getWriter().write(responseBody);
        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    }

    /**
     * Checks whether the provided email address is valid (i.e. matches the regex).
     * The regex is taken from <a href="https://www.baeldung.com/java-email-validation-regexn">Baeldung</a>.
     *
     * @param emailAddress      the email address to validate
     * @return                  true if the provided email address is valid, false otherwise
     */
    private boolean isEmailValid(String emailAddress) {
        String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(emailRegex).matcher(emailAddress).matches();
    }

    /**
     * Gets the JWT from the HTTP request, validates and parses it, and extracts the JWT claims.
     * The JWT is assumed to be in the "Authorization" header and start with "Bearer ".
     *
     * @param request           the HTTP request with the JWT (aka the 'Bearer token')
     * @return                  JWT claims with the user details (userID and role) and JWT details
     */
    private Jws<Claims> resolveJwtClaims(HttpServletRequest request) {
        Optional<String> resolvedJwt = this.jwtProvider.getJwtFromHeader(request);
        if (resolvedJwt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT is missing");
        }
        String jwt = resolvedJwt.get();
        Optional<Jws<Claims>> claims = this.jwtProvider.validateAndParseClaims(jwt);
        if (claims.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT invalid or expired");
        }
        return claims.get();
    }

    /**
     * Creates and registers a new user based on the data specified in the JSON body of the request.
     * Only admin is allowed to perform this operation, which will be checked using the JWT.
     *
     * @param request           the HTTP request with the JWT and the data about the new user
     * @throws IOException      if something goes wrong during the JSON deserialization process
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerNewUser(HttpServletRequest request) throws IOException {
        Jws<Claims> jwtClaims = this.resolveJwtClaims(request);
        if (this.jwtProvider.getRole(jwtClaims) != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operation not allowed");
        }

        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        String email = jsonNode.get("email").asText();
        if (!isEmailValid(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email address is not valid");
        }
        String firstName  = jsonNode.get("first_name").asText();
        String lastName = jsonNode.get("last_name").asText();
        String affiliation = jsonNode.get("affiliation").asText();

        this.authenticationService.registerNewUser(email, firstName, lastName, affiliation);
    }

    /**
     * Retrieves all researchers that are stored in the database.
     * For each researcher, their userID, first name, last name, email and affiliation are returned.
     * Only admin is allowed to perform this operation, which will be checked using the JWT.
     * The JWT is assumed to be in the "Authorization" header and start with "Bearer ".
     *
     * @param request           the HTTP request with the JWT and the data about the new user
     * @param response          the HTTP response with the found researchers that will be sent back
     * @throws IOException      if something goes wrong during the JSON deserialization process
     */
    @GetMapping("/researchers")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getAllResearchers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Jws<Claims> jwtClaims = this.resolveJwtClaims(request);
        if (this.jwtProvider.getRole(jwtClaims) != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operation not allowed");
        }

        List<Researcher> researchers = this.authenticationService.getAllResearchers();
        String responseBody = this.objectMapper
                .writerWithDefaultPrettyPrinter().writeValueAsString(researchers);
        response.getWriter().write(responseBody);
    }

    // @GetMapping("researchers")
    // public void getFilteredResearchers() {}

    /**
     * Updates the personal details of the user with the given userID,
     *  which should be passed as a query parameter user_id.
     * The personal details to be updated are first name, last name and affiliation,
     *  which should be passed in a JSON body (first_name, last_name and affiliation).
     * In case only one of the fields is to be updated, all three have to be specified,
     *  with the same (old) values for the fields that remain the same.
     * Only admin is allowed to perform this operation, which will be checked using the JWT.
     * The JWT is assumed to be in the "Authorization" header and start with "Bearer ".
     *
     * @param request           the HTTP request with the JWT and the data about the user
     * @throws IOException      if something goes wrong during the JSON deserialization process
     */
    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public void changePersonalDetails(HttpServletRequest request) throws IOException {
        Jws<Claims> jwtClaims = this.resolveJwtClaims(request);
        if (this.jwtProvider.getRole(jwtClaims) != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operation not allowed");
        }

        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        String firstName = jsonNode.get("first_name").asText();
        String lastName = jsonNode.get("last_name").asText();
        String affiliation = jsonNode.get("affiliation").asText();

        UUID userId;
        try {
            userId = UUID.fromString(request.getParameter("user_id"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        this.authenticationService.updateUser(userId, firstName, lastName, affiliation);
    }

    /**
     * Deletes the user with the given userID, which should be passed as a query parameter user_id.
     * Only admin is allowed to perform this operation, which will be checked using the JWT.
     * The JWT is assumed to be in the "Authorization" header and start with "Bearer ".
     *
     * @param request           the HTTP request with the JWT and the userID of the user
     */
    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(HttpServletRequest request) {
        Jws<Claims> jwtClaims = this.resolveJwtClaims(request);
        if (this.jwtProvider.getRole(jwtClaims) != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operation not allowed");
        }

        UUID userId;
        try {
            userId = UUID.fromString(request.getParameter("user_id"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        this.authenticationService.deleteUser(userId);
    }

    // @PutMapping("/change_password")
    // public void changePassword(HttpServletRequest request, HttpServletResponse response) {}
}
