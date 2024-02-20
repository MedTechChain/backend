package nl.tudelft.healthblocks.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.tudelft.healthblocks.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    AuthenticationService authenticationService;

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Get JWT from the Authorization header
        final String authenticationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authenticationHeader == null || !authenticationHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authenticationHeader.substring("Bearer ".length());

        // If the user is not authenticated, validate the JWT
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<Jws<Claims>> claims = this.jwtProvider.validateAndParseClaims(jwt);
            // If the JWT is valid, set the user to 'authenticated'
            if (claims.isPresent()) {
                final long userId = this.jwtProvider.getUserId(claims.get());
                UserDetails user = this.authenticationService.loadUserByUserId(userId); // TODO: maybe change to username?

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
