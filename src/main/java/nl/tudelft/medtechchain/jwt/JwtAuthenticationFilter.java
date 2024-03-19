package nl.tudelft.medtechchain.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import nl.tudelft.medtechchain.controllers.ApiEndpoints;
import nl.tudelft.medtechchain.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * A class that represents a custom authentication filter based on JWT.
 */
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    private final JwtProvider jwtProvider;

    /**
     * Performs the authentication step using a JWT (if present).
     * If the authentication has been successful, the principal, the (cleared) credentials and
     *  the authorities are set for the authenticated user, see: <a href="https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-providermanager">AuthenticationProviderManager</a>.
     * If the authentication has failed, the request is passed to the next filter in the chain.
     *
     * @param request               the received HTTP request
     * @param response              the HTTP response to be sent back
     * @param filterChain           filter chain which intercepts and processes incoming requests
     * @throws ServletException     if something goes wrong in the filter chain
     * @throws IOException          if something goes wrong in the filter chain
     */
    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        Optional<String> resolvedJwt = this.jwtProvider.getJwtFromHeader(request);
        if (resolvedJwt.isEmpty()) {
            // Only LOGIN and CHANGE_PASSWORD endpoints can be accessed without JWT
            // If any other endpoint is accessed without JWT, then 401 status code is returned
            if (ApiEndpoints.NO_JWT_PATHS.contains(request.getRequestURI())) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("No Bearer token");
            }
            return;
        }
        String jwt = resolvedJwt.get();

        Jws<Claims> claims;
        try {
            claims = this.jwtProvider.parseClaims(jwt);
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(e.getMessage());
            return;
        }

        UUID userId = this.jwtProvider.getUserId(claims);
        UserDetails user = this.authenticationService.loadUserByUserId(userId);

        // Set the user as authenticated
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
