package nl.tudelft.medtechchain.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.tudelft.medtechchain.jwt.JwtProvider;
import nl.tudelft.medtechchain.models.Researcher;
import nl.tudelft.medtechchain.models.UserData;
import nl.tudelft.medtechchain.services.AuthenticationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


/**
 * A controller class that provides API endpoints for managing users
 *  and interacts with the AuthenticationService class.
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
        String jwt = this.jwtProvider
                .generateJwtToken(user.getUserId(), user.getRole(), new Date());

        String responseBody = this.objectMapper.createObjectNode()
                .put("jwt", jwt)
                .put("token_type", "JWT")
                .put("expires_in", this.jwtProvider.getJwtExpirationTime()).toString();
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        response.getWriter().write(responseBody);
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
        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        String email = jsonNode.get("email").asText();
        String firstName  = jsonNode.get("first_name").asText();
        String lastName = jsonNode.get("last_name").asText();
        String affiliation = jsonNode.get("affiliation").asText();

        this.authenticationService.registerNewUser(email, firstName, lastName, affiliation);
    }

    /**
     * Retrieves all researchers that are stored in the database.
     * For each researcher, their userID, first name, last name, email and affiliation are returned.
     * Only admin is allowed to perform this operation, which will be checked with the JWT
     *  (Spring Security performs the actual authorization check in AuthorizationFilter).
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
        List<Researcher> researchers = this.authenticationService.getAllResearchers();
        String responseBody = this.objectMapper
                .writerWithDefaultPrettyPrinter().writeValueAsString(researchers);
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        response.getWriter().write(responseBody);
    }

    /**
     * Updates the personal details of the user with the given userID,
     *  which should be passed as a query parameter user_id.
     * The personal details to be updated are first name, last name and affiliation,
     *  which should be passed in a JSON body (first_name, last_name and affiliation).
     * In case only one of the fields is to be updated, all three have to be specified,
     *  with the same (old) values for the fields that remain the same.
     * Only admin is allowed to perform this operation, which will be checked using the JWT
     *  (Spring Security performs the actual authorization check in AuthorizationFilter).
     * The JWT is assumed to be in the "Authorization" header and start with "Bearer ".
     *
     * @param request           the HTTP request with the JWT and the data about the user
     * @throws IOException      if something goes wrong during the JSON deserialization process
     */
    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public void changePersonalDetails(HttpServletRequest request) throws IOException {
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
     * Only admin is allowed to perform this operation, which will be checked using the JWT
     *  (Spring Security performs the actual authorization check in AuthorizationFilter).
     * The JWT is assumed to be in the "Authorization" header and start with "Bearer ".
     *
     * @param request           the HTTP request with the JWT and the userID of the user
     */
    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(HttpServletRequest request) {
        UUID userId;
        try {
            userId = UUID.fromString(request.getParameter("user_id"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        this.authenticationService.deleteUser(userId);
    }

    /**
     * Sends a custom exception when data constraints have been violated (e.g. unique, null etc.).
     *
     * @param e                 the thrown DataIntegrityViolationException
     * @return                  HTTP response with the status 400 Bad Request and the error message
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<Object> dataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /**
     * Sends a custom exception when the specified user already exists (e.g. during registration).
     *
     * @param e                 the thrown EntityExistsException
     * @return                  HTTP response with the status 409 Conflict and the error message
     */
    @ExceptionHandler(EntityExistsException.class)
    private ResponseEntity<Object> entityExists(EntityExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    /**
     * Sends a custom exception when the specified user has not been found (e.g. when updating).
     *
     * @param e                 the thrown EntityNotFoundException
     * @return                  HTTP response with the status 404 Not Found and the error message
     */
    @ExceptionHandler(EntityNotFoundException.class)
    private ResponseEntity<Object> entityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
