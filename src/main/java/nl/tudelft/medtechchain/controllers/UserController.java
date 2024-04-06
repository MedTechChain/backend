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
import org.springframework.security.authentication.BadCredentialsException;
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


/**
 * A controller class that provides API endpoints for managing users
 *  and interacts with the AuthenticationService class.
 */
@RestController
@RequestMapping(ApiEndpoints.USERS_API_PREFIX)
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationManager authenticationManager;

    private final AuthenticationService authenticationService;

    private final JwtProvider jwtProvider;

    private final ObjectMapper objectMapper;

    /**
     * Checks whether the given JSON node has all the specified fields.
     * If at least one field is not present in the JSON node,
     *  a ResponseStatusException is thrown with the status code 400 Bad Request.
     *
     * @param jsonNode          a JSON node representing the JSON object
     * @param fields            fields to be checked
     * @return                  true if at least one field is missing in the JSON node
     */
    private boolean hasMissingFields(JsonNode jsonNode, String... fields) {
        for (String field : fields) {
            if (!jsonNode.hasNonNull(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logs in the user. If successful, sends back a JSON with JWT.
     * Both admin and researchers are allowed to perform this operation.
     *
     * @param request           the HTTP request that has been received form the client
     * @param response          the HTTP response that will be sent back
     * @throws IOException      if something goes wrong with the servlets
     */
    @PostMapping(ApiEndpoints.LOGIN)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void login(HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        if (this.hasMissingFields(jsonNode, "username", "password")) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing fields in JSON body");
            return;
        }
        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();

        this.authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));

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
     * @param request           the HTTP request that has been received form the client
     * @param response          the HTTP response that will be sent back
     * @throws IOException      if something goes wrong with the servlets
     */
    @PostMapping(ApiEndpoints.REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    public void registerNewUser(HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        if (this.hasMissingFields(jsonNode, "email", "first_name", "last_name", "affiliation")) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing fields in JSON body");
            return;
        }
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
     * @param response          the HTTP response that will be sent back
     * @throws IOException      if something goes wrong with the servlets
     */
    @GetMapping(ApiEndpoints.GET_RESEARCHERS)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getAllResearchers(HttpServletResponse response) throws IOException {
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
     * @param request           the HTTP request that has been received form the client
     * @param response          the HTTP response that will be sent back
     * @throws IOException      if something goes wrong with the servlets
     */
    @PutMapping(ApiEndpoints.UPDATE)
    @ResponseStatus(HttpStatus.OK)
    public void updatePersonalDetails(HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {
        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        if (this.hasMissingFields(jsonNode, "first_name", "last_name", "affiliation")) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing fields in JSON body");
            return;
        }
        String firstName = jsonNode.get("first_name").asText();
        String lastName = jsonNode.get("last_name").asText();
        String affiliation = jsonNode.get("affiliation").asText();

        UUID userId;
        try {
            userId = UUID.fromString(request.getParameter("user_id"));
        } catch (Exception e) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid UUID in the request");
            return;
        }
        this.authenticationService.updateUser(userId, firstName, lastName, affiliation);
    }

    /**
     * Deletes the user with the given userID, which should be passed as a query parameter user_id.
     * Only admin is allowed to perform this operation, which will be checked using the JWT
     *  (Spring Security performs the actual authorization check in AuthorizationFilter).
     * The JWT is assumed to be in the "Authorization" header and start with "Bearer ".
     *
     * @param request           the received HTTP request
     * @param response          the HTTP response that will be sent back
     * @throws IOException      if something goes wrong with the servlets
     */
    @DeleteMapping(ApiEndpoints.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        UUID userId;
        try {
            userId = UUID.fromString(request.getParameter("user_id"));
        } catch (Exception e) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid UUID in the request");
            return;
        }
        this.authenticationService.deleteUser(userId);
    }

    /**
     * Changes the password of the specified user (defined by the username in the request body).
     * Everyone is allowed to perform this operation, however the provided password for the
     *  specified username must match with the password stored in the database.
     * If the specified username is incorrect (i.e. the user with the specified username cannot be
     *  found), then the status code 401 Unauthorized is returned (done by Spring Security).
     *
     * @param request           the received HTTP request
     * @param response          the HTTP response that will be sent back
     * @throws IOException      if something goes wrong with the servlets
     */
    @PutMapping(ApiEndpoints.CHANGE_PASSWORD)
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        if (this.hasMissingFields(jsonNode, "username", "old_password", "new_password")) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing fields in JSON body");
        }
        String username = jsonNode.get("username").asText();
        String oldPassword = jsonNode.get("old_password").asText();
        String newPassword = jsonNode.get("new_password").asText();

        this.authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));

        this.authenticationService.changePassword(username, oldPassword, newPassword);
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

    /**
     * Sends a custom exception when the provided credentials of a user are incorrect.
     *
     * @param e                 the thrown BadCredentialsException
     * @return                  HTTP response with the status 401 Unauthorized and the error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    private ResponseEntity<Object> badCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
}
