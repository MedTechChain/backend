package nl.tudelft.healthblocks.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import nl.tudelft.healthblocks.service.AuthenticationService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * A class that represents a custom authentication filter based on JWT.
 */
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    private final JwtProvider jwtProvider;

    /**
     * Performs the authentication step using a JWT (if present).
     * If the authentication is successful, the principal, the (cleared) credentials and the authorities are set for the authenticated user,
     *  see: <a href="https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-providermanager">AuthenticationProviderManager</a>.
     * If the authentication fails, the request is passed to the next filter in the SecurityFilterChain.
     *
     * @param request               the received HTTP request
     * @param response              the HTTP response to be sent back
     * @param filterChain           Spring Security filter chain which intercepts and processes incoming requests
     * @throws ServletException     if something goes wrong in the filter chain
     * @throws IOException          if something goes wrong in the filter chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<String> resolvedJwt = this.jwtProvider.getJwtFromHeader(request);
        if (resolvedJwt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = resolvedJwt.get();

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<Jws<Claims>> claims = this.jwtProvider.validateAndParseClaims(jwt);
            if (claims.isPresent()) {
                UUID userId = this.jwtProvider.getUserId(claims.get());
                UserDetails user = this.authenticationService.loadUserByUserId(userId);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
