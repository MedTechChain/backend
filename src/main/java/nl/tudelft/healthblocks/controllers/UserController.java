package nl.tudelft.healthblocks.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nl.tudelft.healthblocks.jwt.JwtProvider;
import nl.tudelft.healthblocks.entities.UserData;
import nl.tudelft.healthblocks.security.UserRole;
import nl.tudelft.healthblocks.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationManager authenticationManager;

    private final AuthenticationService authenticationService;

    private final JwtProvider jwtProvider;

    private final ObjectMapper objectMapper;


    @GetMapping
    public ResponseEntity<String> greetings() {
        return ResponseEntity.ok("Salut, Alin!");
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();

        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        UserData user = this.authenticationService.loadUserByUsername(username);
        String jwt = this.jwtProvider.generateJwtToken(user.getUserId(), user.getRole(), new Date(System.currentTimeMillis()));

        String responseBody = this.objectMapper.createObjectNode()
                .put("jwt", jwt).put("token_type", "JWT")
                .put("expires_in", this.jwtProvider.getJwtExpirationTime()).toString();
        response.getWriter().write(responseBody);
        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    }

    private boolean validateEmail(String emailAddress) {
        // Taken from: https://www.baeldung.com/java-email-validation-regexn
        String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(emailRegex).matcher(emailAddress).matches();
    }

    private Jws<Claims> resolveJwtToken(HttpServletRequest request) {
        Optional<String> resolvedJwt = this.jwtProvider.getJwtFromHeader(request);
        if (resolvedJwt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String jwt = resolvedJwt.get();
        Optional<Jws<Claims>> claims = this.jwtProvider.validateAndParseClaims(jwt);
        if (claims.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT invalid or has expired");
        }
        return claims.get();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerNewUser(HttpServletRequest request) throws IOException {
        JsonNode jsonNode = this.objectMapper.readTree(request.getInputStream());
        String firstName  = jsonNode.get("first_name").asText();
        String lastName = jsonNode.get("last_name").asText();
        String email = jsonNode.get("email").asText();
        String affiliation = jsonNode.get("affiliation").asText();

        if (!validateEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided email address is not valid");
        }
        // TODO: register user in AuthenticationService
        // this.authenticationService.registerNewUser()

        // TODO: Send an email with a password

    }

    @GetMapping("/researchers")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void getAllResearchers(HttpServletRequest request, HttpServletResponse response) {
        Jws<Claims> jwtClaims = this.resolveJwtToken(request);
        if (this.jwtProvider.getRole(jwtClaims) != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Operation not allowed");
        }

        // TODO: fetch from AuthenticationService
        this.authenticationService.getAllResearchers();
    }

    // @GetMapping("researchers")
    // public void getFilteredResearchers() {}

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(HttpServletRequest request, HttpServletResponse response) {
        Jws<Claims> jwtClaims = this.resolveJwtToken(request);
        if (this.jwtProvider.getRole(jwtClaims) != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Operation not allowed");
        }
        String username = request.getParameter("user_id");
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        // TODO: delete from AuthenticationService

    }

    // @PutMapping("/change_password")
    // public void changePassword(HttpServletRequest request, HttpServletResponse response) {}

    // @PutMapping("/change_details")
    // public void changePersonalDetails() {}
}
