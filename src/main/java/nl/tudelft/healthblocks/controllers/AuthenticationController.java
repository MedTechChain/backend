package nl.tudelft.healthblocks.controllers;

import lombok.RequiredArgsConstructor;
import nl.tudelft.healthblocks.dto.AuthenticationRequest;
import nl.tudelft.healthblocks.jwt.JwtProvider;
import nl.tudelft.healthblocks.entities.UserData;
import nl.tudelft.healthblocks.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;

    private PasswordEncoder passwordEncoder;

    private final AuthenticationService authenticationService;

    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody AuthenticationRequest request) {
        System.out.println(request);
        this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        final UserData user = this.authenticationService.loadUserByUsername(request.getUsername());
        if (user != null) {
            return ResponseEntity.ok(this.jwtProvider.generateJwtToken(user.getUserId(), user.getRole(), new Date(System.currentTimeMillis())));
        }
        return ResponseEntity.status(401).body("Error has occurred");
    }
}
